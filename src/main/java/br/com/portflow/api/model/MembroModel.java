package br.com.portflow.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import br.com.portflow.domain.model.Atribuicao;

public class MembroModel {

    @Schema(name = "MembroSaveRequest")
    public record SaveVO(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotNull(message = "Atribuição é obrigatória") Atribuicao atribuicao
    ) {}

    @Schema(name = "MembroResponse")
    public record DTO(
            Long id,
            String nome,
            Atribuicao atribuicao
    ) {}

    @Schema(name = "MembroListResponse")
    public record ListDTO(
            Long id,
            String nome,
            Atribuicao atribuicao
    ) {}
}
