package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.constraints.NotNull;

public record CriteriaAnswerDto(

        Long id,

        @NotNull(message = "Criteria id must not be null")
        Long criteriaId,
        Integer value,

        @NotNull(message = "Team id must not be null")
        Long teamId,

        @NotNull(message = "User id must not be null")
        Long userId
) {
}
