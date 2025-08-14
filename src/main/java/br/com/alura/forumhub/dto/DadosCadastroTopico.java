package br.com.alura.forumhub.dto;

import jakarta.validation.constraints.NotBlank;

public record DadosCadastroTopico(
        @NotBlank(message = "Título é obrigatório")
        String titulo,
        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem,
        @NotBlank(message = "Nome do autor é obrigatório")
        String autor,
        @NotBlank(message = "Nome do curso é obrigatório")
        String curso
) {
}
