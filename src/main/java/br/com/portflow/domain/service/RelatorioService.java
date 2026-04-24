package br.com.portflow.domain.service;

import br.com.portflow.api.model.RelatorioModel;
import br.com.portflow.domain.model.Projeto;
import br.com.portflow.domain.model.StatusProjeto;
import br.com.portflow.domain.repository.AssociacaoProjetoMembroRepository;
import br.com.portflow.domain.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final ProjetoRepository projetoRepository;
    private final AssociacaoProjetoMembroRepository associacaoRepository;

    public RelatorioModel.DTO gerarRelatorio() {
        List<Projeto> projetos = projetoRepository.findAll();

        Map<StatusProjeto, Long> qtdPorStatus = projetos.stream()
                .collect(Collectors.groupingBy(Projeto::getStatusAtual, Collectors.counting()));

        Map<StatusProjeto, BigDecimal> totalOrcadoPorStatus = projetos.stream()
                .collect(Collectors.groupingBy(
                        Projeto::getStatusAtual,
                        Collectors.reducing(BigDecimal.ZERO, Projeto::getOrcamentoTotal, BigDecimal::add)
                ));

        List<Projeto> encerrados = projetos.stream()
                .filter(p -> p.getStatusAtual() == StatusProjeto.ENCERRADO && p.getDataRealTermino() != null)
                .toList();

        double mediaDuracao = 0;
        if (!encerrados.isEmpty()) {
            long totalDias = encerrados.stream()
                    .mapToLong(p -> ChronoUnit.DAYS.between(p.getDataInicio(), p.getDataRealTermino()))
                    .sum();
            mediaDuracao = (double) totalDias / encerrados.size();
        }

        long totalMembrosUnicos = associacaoRepository.findAll().stream()
                .map(a -> a.getMembro().getId())
                .distinct()
                .count();

        return new RelatorioModel.DTO(
                qtdPorStatus,
                totalOrcadoPorStatus,
                mediaDuracao,
                totalMembrosUnicos
        );
    }
}
