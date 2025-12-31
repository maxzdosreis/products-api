package com.maxzdosreis.products_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExpiredTokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExpiredTokenException() {
        super("Token de reset expirou. Solicite um novo link de reset.");
    }

    public ExpiredTokenException(String message) {
        super(message);
    }
}
