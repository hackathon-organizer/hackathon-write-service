package com.hackathonorganizer.hackathonwriteservice.utils.dto;


import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;

import java.util.Set;

public record UserResponseDto(

        Long id,
        String username,
        String description,
        String keyCloakId,
        Long currentHackathonId,
        Long currentTeamId,
        Set<Tag> tags
) {
}
