package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.constraints.NotNull;

public record CriteriaAnswerDto(
        @NotNull
        Long id,
        @NotNull
        Integer value,
        @NotNull
        Long teamId,
        @NotNull
        Long userId
) {

}
