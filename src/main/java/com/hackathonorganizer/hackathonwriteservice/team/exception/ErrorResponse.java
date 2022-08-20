package com.hackathonorganizer.hackathonwriteservice.team.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String httpStatus, String message,
                            LocalDateTime timeStamp) {

}
