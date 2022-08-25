package com.hackathonorganizer.hackathonwriteservice.team.utils.dto;

import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public record EditUserRequestDto(

        @NotNull
        Long id,

        @NotNull
        Set<TeamInvitation> tags
) {

}
