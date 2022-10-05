package com.hackathonorganizer.hackathonwriteservice.hackathon.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TeamException extends RuntimeException {

    private final HttpStatus httpStatus;

    public TeamException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
