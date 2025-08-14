package br.com.alura.forumhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank(message = "Login é obrigatório.")
        @Email(message = "Formato de email inválido para o login.")
        String login,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 6, message = "Formato de email inválido para o login.")
        String senha) {
}
