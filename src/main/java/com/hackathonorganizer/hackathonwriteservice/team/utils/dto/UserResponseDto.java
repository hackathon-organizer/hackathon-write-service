package com.hackathonorganizer.hackathonwriteservice.team.utils.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String username,
        Set<TeamInvitation> invitations
) {
}
