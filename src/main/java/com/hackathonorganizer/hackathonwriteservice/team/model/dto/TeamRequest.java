package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record TeamRequest(

        Long ownerId,

        @NotEmpty
        String name,

        Boolean isOpen,

        String description,

        @NotNull
        Long hackathonId,

        List<Tag> tags
) {

}
