package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record HackathonRequest(

        @NotEmpty(message = "Hackathon name can not be empty!")
        String name,

        @NotEmpty(message = "Hackathon description can not be empty!")
        String description,

        @NotEmpty(message = "Organizer info can not be empty!")
        String organizerInfo,

        Boolean isActive,

        @NotNull
        OffsetDateTime eventStartDate,

        @NotNull
        OffsetDateTime eventEndDate,

        @NotNull
        Long ownerId
) {
}
