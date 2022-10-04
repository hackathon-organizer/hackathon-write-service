package com.hackathonorganizer.hackathonwriteservice.team.utils.dto;

public record UserMembershipRequest(

        Long currentHackathonId,
        Long currentTeamId
) {

}
