package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record HackathonResponse(
        @NotNull
        Long id,

        @NotEmpty
        String name,

        String logoName,

        @NotEmpty
        String description
) {
}
