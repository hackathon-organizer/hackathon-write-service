package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;

import java.util.List;

public record CriteriaRequest(
        List<CriteriaDto> criteria
) {

}
