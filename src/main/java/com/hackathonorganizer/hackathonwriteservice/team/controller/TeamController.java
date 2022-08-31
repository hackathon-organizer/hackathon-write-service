package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    @Value("${server.port}")
    private String port;

    private final TeamService teamService;

    private final NotificationService notificationService;

    private final SimpMessagingTemplate template;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@RequestBody TeamRequest teamRequest) {

        log.info("Processing new team create request {}", teamRequest);
        return teamService.create(teamRequest);
    }

    @PutMapping("/{id}")
    public TeamResponse edit(@PathVariable Long id, @RequestBody TeamRequest teamRequest) {

        log.info("Processing new team id: {} edit request {}", id,
                teamRequest);
        return teamService.editById(id, teamRequest);
    }



    @PostMapping("/{teamId}/invite/{userId}")
    public void inviteUserToTeam(Long teamId, String userId,
            @RequestBody TeamInvitation teamInvitation) {

        log.info("Processing new team invitation");

        teamService.processInvitation(teamId, userId, teamInvitation);

    }



    @GetMapping("/invites/{id}")
    public void getInvites(String id) {

        teamService.findInvitesByUserId(id);

    }


}
