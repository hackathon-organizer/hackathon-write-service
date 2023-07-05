package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record CriteriaRequest(

        Long id,

        @NotEmpty
        String name,

        @NotNull
        Long hackathonId
) {
}
