package com.hackathonorganizer.hackathonwriteservice.team.exception.handler;

import com.hackathonorganizer.hackathonwriteservice.team.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class MethodArgumentNotValidExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(MethodArgumentNotValidException e) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.toString(),
                e.getMessage(),
                LocalDateTime.now());
    }
}
