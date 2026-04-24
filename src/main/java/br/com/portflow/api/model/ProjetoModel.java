package br.com.portflow.api.model;

import br.com.portflow.domain.model.RiscoProjeto;
import br.com.portflow.domain.model.StatusProjeto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProjetoModel {

    @Schema(name = "ProjetoFilter")
    public record FilterVO(
            String nome,
            StatusProjeto status
    ) {}

    @Schema(name = "ProjetoSaveRequest")
    public record SaveVO(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotNull(message = "Data de início é obrigatória") LocalDate dataInicio,
            @NotNull(message = "Previsão de término é obrigatória") LocalDate previsaoTermino,
            LocalDate dataRealTermino,
            @NotNull(message = "Orçamento total é obrigatório") @PositiveOrZero BigDecimal orcamentoTotal,
            String descricao,
            @NotNull(message = "Gerente é obrigatório") Long gerenteId
    ) {}

    @Schema(name = "ProjetoUpdateRequest")
    public record UpdateVO(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotNull(message = "Data de início é obrigatória") LocalDate dataInicio,
            @NotNull(message = "Previsão de término é obrigatória") LocalDate previsaoTermino,
            LocalDate dataRealTermino,
            @NotNull(message = "Orçamento total é obrigatório") @PositiveOrZero BigDecimal orcamentoTotal,
            String descricao,
            @NotNull(message = "Gerente é obrigatório") Long gerenteId,
            @NotNull(message = "Status atual é obrigatório") StatusProjeto statusAtual
    ) {}

    @Schema(name = "ProjetoResponse")
    public record DTO(
            Long id,
            String nome,
            LocalDate dataInicio,
            LocalDate previsaoTermino,
            LocalDate dataRealTermino,
            BigDecimal orcamentoTotal,
            String descricao,
            StatusProjeto statusAtual,
            RiscoProjeto risco,
            Long gerenteId,
            List<Long> membrosIds
    ) {}

    @Schema(name = "ProjetoListPageResponse")
    public record ListPageDTO(
            Long id,
            String nome,
            LocalDate dataInicio,
            LocalDate previsaoTermino,
            StatusProjeto statusAtual,
            RiscoProjeto risco
    ) {}
}
