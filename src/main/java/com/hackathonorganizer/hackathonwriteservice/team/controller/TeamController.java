package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamVisibilityStatusRequest;
import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/write/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed("USER")
    public TeamResponse createTeam(@RequestBody @Valid TeamRequest teamRequest, Principal principal) {

        return teamService.createTeam(teamRequest, principal);
    }

    @PutMapping("/{id}")
    @RolesAllowed({"TEAM_OWNER", "ORGANIZER"})
    public TeamResponse updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest teamRequest, Principal principal) {

        return teamService.updateTeamById(id, teamRequest, principal);
    }

    @PatchMapping("/{id}")
    @RolesAllowed({"TEAM_OWNER", "ORGANIZER"})
    public boolean openOrCloseTeamForMembers(@PathVariable Long id,
                                             @Valid @RequestBody TeamVisibilityStatusRequest teamVisibilityStatusRequest) {

        return teamService.openOrCloseTeamForMembers(id, teamVisibilityStatusRequest);
    }

    @PostMapping("/{teamId}/invites")
    @RolesAllowed({"USER"})
    public void inviteUserToTeam(@PathVariable("teamId") Long teamId,
                                 @RequestBody Long userId,
                                 @RequestParam("username") String username) {

        teamService.processInvitation(teamId, userId, username);
    }

    @PatchMapping("/{teamId}/invites")
    @RolesAllowed("USER")
    public void updateInvitationStatus(@RequestBody @Valid TeamInvitationDto teamInvitation, Principal principal) {

        teamService.updateInvitationStatus(teamInvitation, principal);
    }

    @PatchMapping("/{teamId}/participants/{userId}")
    @RolesAllowed("USER")
    public void addUserToTeam(@PathVariable("teamId") Long teamId, @PathVariable("userId") Long userId, Principal principal) {

        teamService.addUserToTeam(teamId, userId, principal);
    }
}
