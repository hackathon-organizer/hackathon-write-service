package com.hackathonorganizer.hackathonwriteservice.hackathon.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HackathonException extends RuntimeException {

    private final HttpStatus httpStatus;

    public HackathonException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
