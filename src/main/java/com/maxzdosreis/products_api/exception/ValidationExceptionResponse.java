package com.maxzdosreis.products_api.exception;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public record ValidationExceptionResponse(
        Date timestamp,
        String message,
        String details,
        Map<String, String> errors
) {}
