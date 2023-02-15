package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record CriteriaAnswerRequest(

        @NotNull
        Long id,

        @NotEmpty
        String name,

        @Valid
        CriteriaAnswerDto criteriaAnswer
) {
}