package com.hackathonorganizer.hackathonwriteservice.websocket.service;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Setter
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    public static String userId;

    public void sendTeamInviteNotification(TeamInvitationDto teamInvitation) {

        log.info("Sending invite: {} to user", teamInvitation.id());

        messagingTemplate.convertAndSendToUser(userId, "/topic/private-notifications", teamInvitation);
    }
}
