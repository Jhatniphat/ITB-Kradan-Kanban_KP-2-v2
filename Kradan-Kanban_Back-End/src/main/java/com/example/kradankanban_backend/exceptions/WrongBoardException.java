package com.example.kradankanban_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WrongBoardException extends RuntimeException {
    public WrongBoardException(String message) { super(message); }
    @Override
    public synchronized Throwable fillInStackTrace() { return this; }
}
