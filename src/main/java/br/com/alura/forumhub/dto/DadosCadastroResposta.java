package br.com.alura.forumhub.dto;

import jakarta.validation.constraints.NotBlank;

public record DadosCadastroResposta(
        @NotBlank(message = "A mensagem da resposta não pode estar em branco.")
        String mensagem) {
}
