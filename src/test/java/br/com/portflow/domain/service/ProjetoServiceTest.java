package br.com.portflow.domain.service;

import br.com.portflow.api.mapper.ProjetoMapper;
import br.com.portflow.api.model.MembroModel;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private MembroGateway membroGateway;

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
    void validarTransicaoStatus_DevePermitirApenasProximaEtapa() {
        assertDoesNotThrow(() -> 
            projetoService.validarTransicaoStatus(StatusProjeto.EM_ANALISE, StatusProjeto.ANALISE_REALIZADA)
        );
        assertThrows(IllegalArgumentException.class, () ->
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
        MembroModel.DTO membro = new MembroModel.DTO(1L, "João", Atribuicao.OUTRO);
        when(membroGateway.buscarPorId(1L)).thenReturn(membro);
        
        assertThrows(IllegalArgumentException.class, () -> projetoService.associarMembro(1L, 1L));
    }

    @Test
    void atualizar_DeveLancarExcecaoQuandoStatusExecucaoSemMembro() {
        projeto.setStatusAtual(StatusProjeto.ANALISE_APROVADA);
        projeto.setGerenteId(99L);
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(associacaoRepository.countByProjetoId(1L)).thenReturn(0L);

        var vo = new br.com.portflow.api.model.ProjetoModel.UpdateVO(
                "Projeto Teste",
                projeto.getDataInicio(),
                projeto.getPrevisaoTermino(),
                null,
                projeto.getOrcamentoTotal(),
                "desc",
                99L,
                StatusProjeto.INICIADO
        );

        assertThrows(IllegalArgumentException.class, () -> projetoService.atualizar(1L, vo));
    }

    @Test
    void associarMembro_DeveLancarExcecaoQuandoMembroJaEmTresProjetosAtivos() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        MembroModel.DTO membro = new MembroModel.DTO(1L, "João", Atribuicao.FUNCIONARIO);
        when(membroGateway.buscarPorId(1L)).thenReturn(membro);
        when(associacaoRepository.existsByProjetoIdAndMembroId(1L, 1L)).thenReturn(false);
        when(associacaoRepository.countByProjetoId(1L)).thenReturn(1L);

        Projeto ativo1 = Projeto.builder().statusAtual(StatusProjeto.INICIADO).build();
        Projeto ativo2 = Projeto.builder().statusAtual(StatusProjeto.PLANEJADO).build();
        Projeto ativo3 = Projeto.builder().statusAtual(StatusProjeto.EM_ANDAMENTO).build();
        when(associacaoRepository.findByMembroId(1L)).thenReturn(List.of(
                new AssociacaoProjetoMembro(1L, ativo1, 1L),
                new AssociacaoProjetoMembro(2L, ativo2, 1L),
                new AssociacaoProjetoMembro(3L, ativo3, 1L)
        ));

        assertThrows(IllegalArgumentException.class, () -> projetoService.associarMembro(1L, 1L));
    }
}
