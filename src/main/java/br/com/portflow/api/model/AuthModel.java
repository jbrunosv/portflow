package br.com.portflow.api.model;

import jakarta.validation.constraints.NotBlank;

public class AuthModel {

        public record LoginVO(
                        @NotBlank(message = "Login não pode ser vazio") String login,
                        @NotBlank(message = "Senha não pode ser vazia") String senha) {
        }

        public record NovoUsuarioVO(
                        @NotBlank(message = "Login não pode ser vazio") String login,
                        @NotBlank(message = "Senha não pode ser vazia") String senha,
                        Boolean permissaoLeitura,
                        Boolean permissaoLeituraGravacao) {
        }

        public record TokenDTO(
                        String accessToken,
                        String refreshToken) {
        }
}
