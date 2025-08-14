package br.com.alura.forumhub.dto;

import br.com.alura.forumhub.model.Usuario;

public record DadosDetalhamentoUsuario(
        Long id,
        String login
) {
    public DadosDetalhamentoUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin());
    }
}