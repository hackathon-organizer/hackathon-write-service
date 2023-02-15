package com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto;

public record CriteriaDto(
        Long id,
        String name,
        Long hackathonId
) {
}
