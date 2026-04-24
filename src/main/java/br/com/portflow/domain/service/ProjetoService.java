package br.com.portflow.domain.service;

import br.com.portflow.api.mapper.ProjetoMapper;
import br.com.portflow.api.model.MembroModel;
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
    private final MembroGateway membroGateway;
    private final AssociacaoProjetoMembroRepository associacaoRepository;
    private final ProjetoMapper projetoMapper;

    @Transactional
    public Projeto salvar(ProjetoModel.SaveVO vo) {
        Projeto projeto = projetoMapper.toEntity(vo);
        buscarMembroPorId(vo.gerenteId());
        projeto.setGerenteId(vo.gerenteId());
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

        if (!projeto.getGerenteId().equals(vo.gerenteId())) {
            buscarMembroPorId(vo.gerenteId());
            projeto.setGerenteId(vo.gerenteId());
        }

        projeto.setStatusAtual(vo.statusAtual());
        validarMinimoMembrosParaStatus(projeto);
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

        StatusProjeto proximoStatus = obterProximoStatus(atual);
        if (proximoStatus != novo) {
            throw new IllegalArgumentException(
                    "Não é permitido pular etapas do fluxo. Transição inválida de " + atual + " para " + novo);
        }
    }

    private StatusProjeto obterProximoStatus(StatusProjeto statusAtual) {
        return switch (statusAtual) {
            case EM_ANALISE -> StatusProjeto.ANALISE_REALIZADA;
            case ANALISE_REALIZADA -> StatusProjeto.ANALISE_APROVADA;
            case ANALISE_APROVADA -> StatusProjeto.INICIADO;
            case INICIADO -> StatusProjeto.PLANEJADO;
            case PLANEJADO -> StatusProjeto.EM_ANDAMENTO;
            case EM_ANDAMENTO -> StatusProjeto.ENCERRADO;
            case ENCERRADO, CANCELADO -> null;
        };
    }

    private void validarMinimoMembrosParaStatus(Projeto projeto) {
        StatusProjeto status = projeto.getStatusAtual();
        if (status == StatusProjeto.INICIADO ||
                status == StatusProjeto.PLANEJADO ||
                status == StatusProjeto.EM_ANDAMENTO ||
                status == StatusProjeto.ENCERRADO) {
            long qtdMembrosNoProjeto = associacaoRepository.countByProjetoId(projeto.getId());
            if (qtdMembrosNoProjeto < 1) {
                throw new IllegalArgumentException(
                        "Projeto deve possuir ao menos 1 membro associado para avançar para status de execução.");
            }
        }
    }

    @Transactional
    public void associarMembro(Long projetoId, Long membroId) {
        Projeto projeto = buscarPorId(projetoId);
        MembroModel.DTO membro = buscarMembroPorId(membroId);

        validarMembroParaAssociacao(projetoId, membro);

        long qtdMembrosNoProjeto = associacaoRepository.countByProjetoId(projetoId);
        if (qtdMembrosNoProjeto >= 10) {
            throw new IllegalArgumentException("O projeto já atingiu o limite máximo de 10 membros.");
        }

        validarLimiteProjetosAtivosDoMembro(membro.id());

        AssociacaoProjetoMembro associacao = new AssociacaoProjetoMembro(null, projeto, membro.id());
        associacaoRepository.save(associacao);
    }

    private MembroModel.DTO buscarMembroPorId(Long membroId) {
        MembroModel.DTO membro = membroGateway.buscarPorId(membroId);
        if (membro == null || membro.id() == null) {
            throw new EntityNotFoundException("Membro não encontrado com ID: " + membroId);
        }
        return membro;
    }

    private void validarMembroParaAssociacao(Long projetoId, MembroModel.DTO membro) {
        if (membro.atribuicao() != Atribuicao.FUNCIONARIO) {
            throw new IllegalArgumentException("Apenas membros com atribuição 'funcionário' podem ser associados.");
        }
        if (associacaoRepository.existsByProjetoIdAndMembroId(projetoId, membro.id())) {
            throw new IllegalArgumentException("Membro já está associado a este projeto.");
        }
    }

    private void validarLimiteProjetosAtivosDoMembro(Long membroId) {
        List<AssociacaoProjetoMembro> associacoesDoMembro = associacaoRepository.findByMembroId(membroId);
        long projetosAtivos = associacoesDoMembro.stream()
                .filter(a -> a.getProjeto().getStatusAtual() != StatusProjeto.ENCERRADO &&
                        a.getProjeto().getStatusAtual() != StatusProjeto.CANCELADO)
                .count();

        if (projetosAtivos >= 3) {
            throw new IllegalArgumentException(
                    "Membro não pode estar alocado em mais de 3 projetos simultaneamente (excluindo encerrados/cancelados).");
        }
    }
}
