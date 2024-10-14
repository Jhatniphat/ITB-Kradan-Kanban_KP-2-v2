package com.example.kradankanban_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConfilctException extends RuntimeException {
    public ConfilctException(String message) {
        super(message);
    }
}
