package com.hackathonorganizer.hackathonwriteservice.websocket.service;

import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.utils.TeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendTeamInviteNotification(TeamInvitation teamInvitation) {

        TeamInvitationDto inviteDto = TeamMapper.mapToTeamInvitationDto(teamInvitation);

        log.info("Sending invite with id: {} to user with id {}", inviteDto.id(), teamInvitation.getToUserId());

        messagingTemplate.convertAndSendToUser(String.valueOf(teamInvitation.getToUserId()),
                "/topic/invitations", inviteDto);
    }
}
