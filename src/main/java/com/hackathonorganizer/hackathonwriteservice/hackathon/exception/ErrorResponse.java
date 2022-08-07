package com.hackathonorganizer.hackathonwriteservice.hackathon.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record ErrorResponse (
    String message,
    List<String> details
) {
}
