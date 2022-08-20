package com.hackathonorganizer.hackathonwriteservice.team.exception.handler;

import com.hackathonorganizer.hackathonwriteservice.team.exception.ErrorResponse;
import com.hackathonorganizer.hackathonwriteservice.team.exception.ResourceAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ResourceAlreadyExistsExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(ResourceAlreadyExistsException e) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.toString(),
                e.getMessage(),
                LocalDateTime.now());
    }

}
