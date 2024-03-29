package com.hackathonorganizer.hackathonwriteservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TeamException extends BaseException {

    public TeamException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
