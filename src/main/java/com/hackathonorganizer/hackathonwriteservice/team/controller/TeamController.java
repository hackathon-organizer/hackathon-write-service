package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/write/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@RequestBody TeamRequest teamRequest) {

        log.info("Processing new team create request {}", teamRequest);
        return teamService.create(teamRequest);
    }

    @PutMapping("/{id}")
    public TeamResponse edit(@PathVariable Long id, @RequestBody TeamRequest teamRequest) {

        log.info("Processing new team id: {} edit request {}", id, teamRequest);
        return teamService.editById(id, teamRequest);
    }

    @PatchMapping("/{id}")
    public boolean openOrCloseTeamForMembers(@PathVariable Long id,
            @RequestBody boolean isOpen) {

        log.info("Processing team with id: {} open/close", id);
        return teamService.openOrCloseTeamForMembers(id, isOpen);
    }

    @PostMapping("/{teamId}/invite/{userId}")
    public void inviteUserToTeam(@PathVariable("teamId") Long teamId,
            @PathVariable("userId") Long userId, @RequestParam("username") String username) {

        log.info("Processing new team invitation");
        teamService.processInvitation(teamId, userId, username);
    }

    @PatchMapping("/{teamId}/invites")
    public void updateInvitationStatus(@RequestBody TeamInvitationDto teamInvitation) {

        log.info("Updating team: {} invitation status", teamInvitation.teamId());
        teamService.updateInvitationStatus(teamInvitation);
    }

    @PatchMapping("/{id}/participants/{userId}")
    public void addUserToTeam(@PathVariable("id") Long teamId,
            @PathVariable("userId") Long userId) {

        teamService.addUserToTeam(teamId, userId);
    }
}
