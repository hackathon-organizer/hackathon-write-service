package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;

public record TeamInvitationDto(

        Long id,
        String fromUserName,
        Long toUserId,
        InvitationStatus invitationStatus,
        String teamName,
        Long teamId

) {
}
