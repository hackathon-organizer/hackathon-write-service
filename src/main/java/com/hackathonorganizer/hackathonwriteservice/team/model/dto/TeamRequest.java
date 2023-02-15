package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record TeamRequest(

        @NotNull
        Long ownerId,

        @NotEmpty(message = "Name can not be empty!")
        String name,

        Boolean isOpen,

        @NotEmpty(message = "Description can not be empty!")
        String description,

        @NotNull(message = "Hackathon can not be null!")
        Long hackathonId,

        List<Tag> tags
) {

}
