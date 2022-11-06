package com.hackathonorganizer.hackathonwriteservice.websocket.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final NotificationService notificationService;

    @MessageMapping("/private-message")
    @SendToUser("/topic/private-messages")
    public void inviteUserToTeam(TeamInvitation teamInvitation) {

        notificationService.sendTeamInviteNotification(teamInvitation);
    }
}
