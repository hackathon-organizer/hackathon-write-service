package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public record CriteriaRequest(
        @NotNull
        List<CriteriaDto> criteria
) {

}
