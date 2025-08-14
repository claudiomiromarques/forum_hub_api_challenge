package br.com.alura.forumhub.infra.exception;

public class ValidacaoTokenException extends RuntimeException {
    public ValidacaoTokenException(String message) {
        super(message);
    }
}