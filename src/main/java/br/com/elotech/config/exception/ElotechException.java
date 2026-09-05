package br.com.elotech.config.exception;

import org.springframework.http.HttpStatus;

public class ElotechException extends RuntimeException {

    private static final long serialVersionUID = -4322853684363605002L;

	private final HttpStatus status;

    public ElotechException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}