package br.com.portflow.domain.service;

import br.com.portflow.api.mapper.ProjetoMapper;
import br.com.portflow.api.model.ProjetoModel;
import br.com.portflow.domain.model.*;
import br.com.portflow.domain.repository.AssociacaoProjetoMembroRepository;
import br.com.portflow.domain.repository.ProjetoRepository;
import br.com.portflow.domain.specification.ProjetoSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final MembroService membroService;
    private final AssociacaoProjetoMembroRepository associacaoRepository;
    private final ProjetoMapper projetoMapper;

    @Transactional
    public Projeto salvar(ProjetoModel.SaveVO vo) {
        Projeto projeto = projetoMapper.toEntity(vo);
        Membro gerente = membroService.buscarPorId(vo.gerenteId());
        projeto.setGerente(gerente);
        projeto.setStatusAtual(StatusProjeto.EM_ANALISE);
        projeto.setRisco(calcularRisco(projeto));

        return projetoRepository.save(projeto);
    }

    @Transactional
    public Projeto atualizar(Long id, ProjetoModel.UpdateVO vo) {
        Projeto projeto = buscarPorId(id);

        validarTransicaoStatus(projeto.getStatusAtual(), vo.statusAtual());

        projeto.setNome(vo.nome());
        projeto.setDataInicio(vo.dataInicio());
        projeto.setPrevisaoTermino(vo.previsaoTermino());
        projeto.setDataRealTermino(vo.dataRealTermino());
        projeto.setOrcamentoTotal(vo.orcamentoTotal());
        projeto.setDescricao(vo.descricao());

        if (!projeto.getGerente().getId().equals(vo.gerenteId())) {
            Membro gerente = membroService.buscarPorId(vo.gerenteId());
            projeto.setGerente(gerente);
        }

        projeto.setStatusAtual(vo.statusAtual());
        projeto.setRisco(calcularRisco(projeto));

        return projetoRepository.save(projeto);
    }

    @Transactional
    public void excluir(Long id) {
        Projeto projeto = buscarPorId(id);
        if (projeto.getStatusAtual() == StatusProjeto.INICIADO ||
                projeto.getStatusAtual() == StatusProjeto.EM_ANDAMENTO ||
                projeto.getStatusAtual() == StatusProjeto.ENCERRADO) {
            throw new IllegalArgumentException(
                    "Projeto não pode ser excluído no status atual: " + projeto.getStatusAtual());
        }
        projetoRepository.delete(projeto);
    }

    public Projeto buscarPorId(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado com ID: " + id));
    }

    public Page<Projeto> listarComFiltros(ProjetoModel.FilterVO filterVO, Pageable pageable) {
        Specification<Projeto> spec = ProjetoSpecification.comFiltros(filterVO);
        return projetoRepository.findAll(spec, pageable);
    }

    public RiscoProjeto calcularRisco(Projeto projeto) {
        long prazoMeses = ChronoUnit.MONTHS.between(projeto.getDataInicio(), projeto.getPrevisaoTermino());
        BigDecimal orcamento = projeto.getOrcamentoTotal();

        if (orcamento.compareTo(BigDecimal.valueOf(500_000)) > 0 || prazoMeses > 6) {
            return RiscoProjeto.ALTO;
        } else if (orcamento.compareTo(BigDecimal.valueOf(100_000)) > 0 || prazoMeses > 3) {
            return RiscoProjeto.MEDIO;
        }

        return RiscoProjeto.BAIXO;
    }

    public void validarTransicaoStatus(StatusProjeto atual, StatusProjeto novo) {
        if (atual == novo || novo == StatusProjeto.CANCELADO) {
            return;
        }

        if (novo.ordinal() < atual.ordinal()) {
            throw new IllegalArgumentException(
                    "Não é permitido retroceder o status do projeto. Transição inválida de " + atual + " para " + novo);
        }
    }

    @Transactional
    public void associarMembro(Long projetoId, Long membroId) {
        Projeto projeto = buscarPorId(projetoId);
        Membro membro = membroService.buscarPorId(membroId);

        if (membro.getAtribuicao() != Atribuicao.FUNCIONARIO) {
            throw new IllegalArgumentException("Apenas membros com atribuição 'funcionário' podem ser associados.");
        }

        if (associacaoRepository.existsByProjetoIdAndMembroId(projetoId, membroId)) {
            throw new IllegalArgumentException("Membro já está associado a este projeto.");
        }

        long qtdMembrosNoProjeto = associacaoRepository.countByProjetoId(projetoId);
        if (qtdMembrosNoProjeto >= 10) {
            throw new IllegalArgumentException("O projeto já atingiu o limite máximo de 10 membros.");
        }

        List<AssociacaoProjetoMembro> associacoesDoMembro = associacaoRepository.findByMembroId(membroId);
        long projetosAtivos = associacoesDoMembro.stream()
                .filter(a -> a.getProjeto().getStatusAtual() != StatusProjeto.ENCERRADO &&
                        a.getProjeto().getStatusAtual() != StatusProjeto.CANCELADO)
                .count();

        if (projetosAtivos >= 3) {
            throw new IllegalArgumentException(
                    "Membro não pode estar alocado em mais de 3 projetos simultaneamente (excluindo encerrados/cancelados).");
        }

        AssociacaoProjetoMembro associacao = new AssociacaoProjetoMembro(null, projeto, membro);
        associacaoRepository.save(associacao);
    }
}
