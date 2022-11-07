package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/write/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@RequestBody TeamRequest teamRequest) {

        return teamService.createTeam(teamRequest);
    }

    @PutMapping("/{id}")
    public TeamResponse editTeam(@PathVariable Long id, @RequestBody TeamRequest teamRequest) {

        return teamService.editTeamById(id, teamRequest);
    }

    @PatchMapping("/{id}")
    public boolean openOrCloseTeamForMembers(@PathVariable Long id, @RequestBody boolean isOpen) {

        return teamService.openOrCloseTeamForMembers(id, isOpen);
    }

    @PostMapping("/{teamId}/invite/{userId}")
    public void inviteUserToTeam(@PathVariable("teamId") Long teamId,
            @PathVariable("userId") Long userId, @RequestParam("username") String username) {

        teamService.processInvitation(teamId, userId, username);
    }

    @PatchMapping("/{teamId}/invites")
    public void updateInvitationStatus(@RequestBody TeamInvitationDto teamInvitation) {

        teamService.updateInvitationStatus(teamInvitation);
    }

    @PatchMapping("/{id}/participants/{userId}")
    public void addUserToTeam(@PathVariable("id") Long teamId,
            @PathVariable("userId") Long userId) {

        teamService.addUserToTeam(teamId, userId);
    }
}
