package com.hackathonorganizer.hackathonwriteservice.hackathon.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HackathonException extends BaseException {

    public HackathonException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
