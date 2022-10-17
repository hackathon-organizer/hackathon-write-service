package com.hackathonorganizer.hackathonwriteservice.utils.dto;

public record UserMembershipRequest(

        Long currentHackathonId,
        Long currentTeamId
) {

}
