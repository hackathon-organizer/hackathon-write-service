package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record CriteriaDto(

        Long id,
        @NotEmpty
        String name,
        @NotNull
        Long hackathonId
) {
}
