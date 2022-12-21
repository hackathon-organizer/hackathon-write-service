package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamVisibilityStatusRequest;
import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/write/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolesAllowed({"USER"})
    public TeamResponse createTeam(@RequestBody TeamRequest teamRequest, Principal principal) {
        //TODO add user TEAM_OWNER role

        return teamService.createTeam(teamRequest, principal);
    }

    @PutMapping("/{id}")
    @RolesAllowed({"TEAM_OWNER","ORGANIZER"})
    public TeamResponse editTeam(@PathVariable Long id, @RequestBody TeamRequest teamRequest, Principal principal) {

        return teamService.editTeamById(id, teamRequest, principal);
    }

    @PatchMapping("/{id}")
    @RolesAllowed({"TEAM_OWNER","ORGANIZER"})
    public boolean openOrCloseTeamForMembers(@PathVariable Long id,
                                             @RequestBody TeamVisibilityStatusRequest teamVisibilityStatusRequest) {

        return teamService.openOrCloseTeamForMembers(id, teamVisibilityStatusRequest);
    }

    @PostMapping("/{teamId}/invite/{userId}")
    @RolesAllowed({"USER"})
    public void inviteUserToTeam(@PathVariable("teamId") Long teamId,
            @PathVariable("userId") Long userId, @RequestParam("username") String username, Principal principal) {

        teamService.processInvitation(teamId, userId, username, principal);
    }

    @PatchMapping("/{teamId}/invites")
    @RolesAllowed("USER")
    public void updateInvitationStatus(@RequestBody TeamInvitationDto teamInvitation) {

        teamService.updateInvitationStatus(teamInvitation);
    }

    @PatchMapping("/{id}/participants/{userId}")
    @RolesAllowed({"USER"})
    public void addUserToTeam(@PathVariable("id") Long teamId, @PathVariable("userId") Long userId) {

        teamService.addUserToTeam(teamId, userId);
    }
}
