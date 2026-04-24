package br.com.portflow.domain.service;

import br.com.portflow.api.mapper.ProjetoMapper;
import br.com.portflow.domain.model.*;
import br.com.portflow.domain.repository.AssociacaoProjetoMembroRepository;
import br.com.portflow.domain.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroService membroService;

    @Mock
    private AssociacaoProjetoMembroRepository associacaoRepository;

    @Mock
    private ProjetoMapper projetoMapper;

    @InjectMocks
    private ProjetoService projetoService;

    private Projeto projeto;

    @BeforeEach
    void setUp() {
        projeto = new Projeto();
        projeto.setId(1L);
        projeto.setNome("Projeto Teste");
        projeto.setDataInicio(LocalDate.now());
        projeto.setPrevisaoTermino(LocalDate.now().plusMonths(2));
        projeto.setOrcamentoTotal(new BigDecimal("50000"));
        projeto.setStatusAtual(StatusProjeto.EM_ANALISE);
    }

    @Test
    void calcularRisco_DeveRetornarBaixoRisco() {
        projeto.setOrcamentoTotal(new BigDecimal("99000"));
        projeto.setPrevisaoTermino(LocalDate.now().plusMonths(3));
        
        RiscoProjeto risco = projetoService.calcularRisco(projeto);
        
        assertEquals(RiscoProjeto.BAIXO, risco);
    }

    @Test
    void calcularRisco_DeveRetornarMedioRisco_PorOrcamento() {
        projeto.setOrcamentoTotal(new BigDecimal("150000"));
        projeto.setPrevisaoTermino(LocalDate.now().plusMonths(2));
        
        RiscoProjeto risco = projetoService.calcularRisco(projeto);
        
        assertEquals(RiscoProjeto.MEDIO, risco);
    }

    @Test
    void calcularRisco_DeveRetornarAltoRisco_PorPrazo() {
        projeto.setOrcamentoTotal(new BigDecimal("50000"));
        projeto.setPrevisaoTermino(LocalDate.now().plusMonths(7));
        
        RiscoProjeto risco = projetoService.calcularRisco(projeto);
        
        assertEquals(RiscoProjeto.ALTO, risco);
    }

    @Test
    void validarTransicaoStatus_DeveLancarExcecaoAoRetrocederStatus() {
        assertThrows(IllegalArgumentException.class, () -> 
            projetoService.validarTransicaoStatus(StatusProjeto.ANALISE_APROVADA, StatusProjeto.EM_ANALISE)
        );
    }

    @Test
    void validarTransicaoStatus_DevePermitirPularEtapaParaFrente() {
        assertDoesNotThrow(() -> 
            projetoService.validarTransicaoStatus(StatusProjeto.EM_ANALISE, StatusProjeto.ANALISE_APROVADA)
        );
    }

    @Test
    void validarTransicaoStatus_DevePermitirCanceladoAQualquerMomento() {
        assertDoesNotThrow(() -> 
            projetoService.validarTransicaoStatus(StatusProjeto.EM_ANALISE, StatusProjeto.CANCELADO)
        );
    }

    @Test
    void excluir_DeveLancarExcecaoParaStatusIniciado() {
        projeto.setStatusAtual(StatusProjeto.INICIADO);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        
        assertThrows(IllegalArgumentException.class, () -> projetoService.excluir(1L));
        verify(projetoRepository, never()).delete(any(Projeto.class));
    }

    @Test
    void associarMembro_DeveLancarExcecaoSeNaoForFuncionario() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        
        Membro membro = new Membro(1L, "João", Atribuicao.OUTRO);
        when(membroService.buscarPorId(1L)).thenReturn(membro);
        
        assertThrows(IllegalArgumentException.class, () -> projetoService.associarMembro(1L, 1L));
    }
}
