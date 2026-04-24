package br.com.portflow.api.model;

import br.com.portflow.domain.model.StatusProjeto;
import java.math.BigDecimal;
import java.util.Map;

public class RelatorioModel {
    public record DTO(
            Map<StatusProjeto, Long> quantidadeProjetosPorStatus,
            Map<StatusProjeto, BigDecimal> totalOrcadoPorStatus,
            Double mediaDuracaoDiasProjetosEncerrados,
            Long totalMembrosUnicosAlocados
    ) {}
}
