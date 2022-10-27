package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;

import javax.validation.constraints.NotEmpty;

public record TeamInvitationDto (

    Long id,
    @NotEmpty
    String fromUserName,
    Long toUserId,
    InvitationStatus invitationStatus,
    @NotEmpty
    String teamName,
    Long teamId

) {}
