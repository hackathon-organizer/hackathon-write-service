package com.hackathonorganizer.hackathonwriteservice.team.exception.handler;

import com.hackathonorganizer.hackathonwriteservice.team.exception.ErrorResponse;
import com.hackathonorganizer.hackathonwriteservice.team.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ResourceNotFoundExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(ResourceNotFoundException e) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.toString(),
                e.getMessage(),
                LocalDateTime.now());
    }
}
