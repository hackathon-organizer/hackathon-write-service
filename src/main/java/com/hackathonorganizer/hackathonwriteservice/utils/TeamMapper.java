package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TeamMapper {

    public static TeamResponse mapToTeamDto(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getOwnerId(),
                team.getHackathon().getId(),
                team.getTeamMembersIds(),
                team.getChatRoomId(),
                team.getTags());
    }

    public static TeamInvitationDto mapToTeamInvitationDto(TeamInvitation teamInvitation) {
        return new TeamInvitationDto(
                teamInvitation.getId(),
                teamInvitation.getFromUserName(),
                teamInvitation.getToUserId(),
                InvitationStatus.PENDING,
                teamInvitation.getTeamName(),
                teamInvitation.getTeam().getId());
    }
}
