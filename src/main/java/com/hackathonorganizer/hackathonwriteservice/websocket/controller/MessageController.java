package com.hackathonorganizer.hackathonwriteservice.websocket.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final NotificationService notificationService;


    // @Header("userId") String userId;

    @MessageMapping("/private-message")
    @SendToUser("/topic/private-messages")
    public void inviteUserToTeam(TeamInvitationDto teamInvitation) throws InterruptedException {

        notificationService.sendTeamInviteNotification(teamInvitation);

        log.info("User id: {} send invite to team", NotificationService.userId);
    }
}
