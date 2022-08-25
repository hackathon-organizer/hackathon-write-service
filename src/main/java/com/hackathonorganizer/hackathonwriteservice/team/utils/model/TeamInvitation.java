package com.hackathonorganizer.hackathonwriteservice.team.utils.model;

import javax.validation.constraints.NotNull;

public record TeamInvitation(

        @NotNull
        Long invitedUserId,

        InvitationStatus invitationStatus,

        String teamName,

        @NotNull
        Long teamId
) {

}
