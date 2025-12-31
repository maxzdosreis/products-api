package com.maxzdosreis.products_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenAlreadyUsedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TokenAlreadyUsedException() {
        super("Token já foi utilizado. Solicite um novo link de reset.");
    }

    public TokenAlreadyUsedException(String message) {
        super(message);
    }
}
