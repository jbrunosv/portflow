package br.com.portflow.api.model;

import br.com.portflow.domain.model.RiscoProjeto;
import br.com.portflow.domain.model.StatusProjeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProjetoModel {

    public record FilterVO(
            String nome,
            StatusProjeto status
    ) {}

    public record SaveVO(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotNull(message = "Data de início é obrigatória") LocalDate dataInicio,
            @NotNull(message = "Previsão de término é obrigatória") LocalDate previsaoTermino,
            LocalDate dataRealTermino,
            @NotNull(message = "Orçamento total é obrigatório") @PositiveOrZero BigDecimal orcamentoTotal,
            String descricao,
            @NotNull(message = "Gerente é obrigatório") Long gerenteId
    ) {}

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
            MembroModel.DTO gerente,
            List<MembroModel.DTO> membros
    ) {}

    public record ListPageDTO(
            Long id,
            String nome,
            LocalDate dataInicio,
            LocalDate previsaoTermino,
            StatusProjeto statusAtual,
            RiscoProjeto risco
    ) {}
}
