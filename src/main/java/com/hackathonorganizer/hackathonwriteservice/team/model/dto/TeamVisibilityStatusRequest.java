package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

public record TeamVisibilityStatusRequest (
        Long userId,
        boolean isOpen
) {
}
