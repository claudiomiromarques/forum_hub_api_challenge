package br.com.alura.forumhub.dto;

import br.com.alura.forumhub.model.Resposta;

import java.time.LocalDateTime;

public record DadosListagemResposta(
        Long id,
        String mensagem,
        LocalDateTime dataCriacao,
        String nomeAutor,
        Boolean solucao) {

    public DadosListagemResposta(Resposta resposta) {
        this(
                resposta.getId(),
                resposta.getMensagem(),
                resposta.getDataCriacao(),
                resposta.getAutor().getUsername(),
                resposta.getSolucao());
    }
}
