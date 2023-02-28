package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record TeamInvitationDto(

        @NotNull
        Long id,

        @NotEmpty(message = "Username can not be empty!")
        String fromUserName,

        @NotNull
        Long toUserId,

        @NotNull(message = "Invitation status can not be empty!")
        InvitationStatus invitationStatus,

        @NotEmpty(message = "Name can not be empty!")
        String teamName,

        @NotNull(message = "Team id can not be empty!")
        Long teamId
) {
}
