package com.hackathonorganizer.hackathonwriteservice.websocket.service;

import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.websocket.model.MeetingInvitation;
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

    public void sendTeamInviteNotification(TeamInvitation teamInvitation) throws InterruptedException {

        TeamInvitationDto inviteDto = TeamMapper.mapToTeamInvitationDto(teamInvitation);

        log.info("Sending invite: {} to user", inviteDto.id());

        messagingTemplate.convertAndSendToUser(userId, "/topic/private-notifications", inviteDto);

        sendMeetingNotificationToMentor();
    }

    public void sendMeetingNotificationToMentor() throws InterruptedException {

        MeetingInvitation meetingInvitation = new MeetingInvitation("/team/1/chat");

        for (int i = 0; i < 1000; i++) {
            Thread.sleep(3000);

            log.info("Sending meeting invite: {} to user", 1);

            messagingTemplate.convertAndSendToUser(userId, "/topic/private-messagess", meetingInvitation);
        }


    }
}
