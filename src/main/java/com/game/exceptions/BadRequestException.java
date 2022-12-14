package com.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    //400 error
    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }
}
