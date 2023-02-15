package com.hackathonorganizer.hackathonwriteservice.utils.dto;

public record UserMembershipResponse(

        Long userId,
        Long currentHackathonId,
        Long currentTeamId
) {
}
