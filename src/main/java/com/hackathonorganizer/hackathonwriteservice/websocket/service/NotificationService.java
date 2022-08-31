package com.hackathonorganizer.hackathonwriteservice.websocket.service;

import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Setter
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    public static String userId;

    public void sendTeamInviteNotification(TeamInvitation teamInvitation) {

        messagingTemplate.convertAndSendToUser(userId, "/topic/private-notifications", teamInvitation);
    }
}
