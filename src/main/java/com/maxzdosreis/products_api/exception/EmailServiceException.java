package com.maxzdosreis.products_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmailServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailServiceException() {
        super("Erro ao enviar email. Tente novamente mais tarde.");
    }

    public EmailServiceException(String message) {
        super(message);
    }

    public EmailServiceException(String s, Exception e) {
        super(s, e);
    }
}
