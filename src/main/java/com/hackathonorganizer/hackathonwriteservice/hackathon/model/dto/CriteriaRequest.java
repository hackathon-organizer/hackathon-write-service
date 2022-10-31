package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;

import javax.validation.constraints.NotNull;
import java.util.List;

public record CriteriaRequest(
        @NotNull
        List<CriteriaDto> criteria
) {

}
