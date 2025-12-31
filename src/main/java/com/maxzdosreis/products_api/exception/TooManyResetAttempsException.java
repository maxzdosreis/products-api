package com.maxzdosreis.products_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyResetAttempsException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public TooManyResetAttempsException() {
        super("Muitas tentativas de reset. Tente novamente em alguns minutos.");
    }

    public TooManyResetAttempsException(String message) {
        super(message);
    }
}
