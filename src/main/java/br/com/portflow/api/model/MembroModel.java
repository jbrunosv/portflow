package br.com.portflow.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import br.com.portflow.domain.model.Atribuicao;

public class MembroModel {

    public record SaveVO(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotNull(message = "Atribuição é obrigatória") Atribuicao atribuicao
    ) {}

    public record DTO(
            Long id,
            String nome,
            Atribuicao atribuicao
    ) {}

    public record ListPageDTO(
            Long id,
            String nome,
            Atribuicao atribuicao
    ) {}
}
