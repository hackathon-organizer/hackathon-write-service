package com.hackathonorganizer.hackathonwriteservice.team.model.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record TeamInvitationDto(
        @NotNull
        Long id,
        @NotEmpty
        String fromUserName,
        @NotNull
        Long toUserId,
        @Pattern(regexp = "PENDING|ACCEPTED|REJECTED")
        InvitationStatus invitationStatus,
        @NotEmpty
        String teamName,
        @NotNull
        Long teamId

) {

}
