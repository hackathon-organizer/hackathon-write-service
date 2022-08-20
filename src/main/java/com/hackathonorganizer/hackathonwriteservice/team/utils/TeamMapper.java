package com.hackathonorganizer.hackathonwriteservice.team.utils;

import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TeamMapper {

    public static TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getOwnerId(),
                team.getHackathon().getId(),
                team.getTeamMembersIds(),
                team.getTags());
    }
}
