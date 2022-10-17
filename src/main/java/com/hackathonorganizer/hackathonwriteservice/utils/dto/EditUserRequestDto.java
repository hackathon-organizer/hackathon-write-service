package com.hackathonorganizer.hackathonwriteservice.utils.dto;

import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;

import javax.validation.constraints.NotNull;
import java.util.Set;

public record EditUserRequestDto(

        @NotNull
        Long id,

        @NotNull
        Set<TeamInvitation> tags
) {

}
