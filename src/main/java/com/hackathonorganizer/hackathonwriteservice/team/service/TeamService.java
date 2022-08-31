package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.team.exception.ResourceNotFoundException;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.team.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {


    private final TeamRepository teamRepository;

    private final TagService tagService;

    private final SimpMessagingTemplate messagingTemplate;

    private final HackathonService hackathonService;

    private final NotificationService notificationService;

    public static String userId;

    public TeamResponse create(TeamRequest teamRequest) {

        val teamToSave = Team.builder()
                .ownerId(teamRequest.ownerId())
                .hackathon(getHackathonById(teamRequest.hackathonId()))
                .teamMembersIds(teamRequest.teamMembersIds())
                .tags(teamRequest.tags())
                .build();
        log.info("Trying to save new team {}", teamToSave);
        return TeamMapper.toResponse(teamRepository.save(teamToSave));
    }

    public TeamResponse editById(Long id, TeamRequest teamRequest) {

        return teamRepository.findById(id).map(teamToEdit -> {
            teamToEdit.setOwnerId(teamRequest.ownerId());
            teamToEdit.setHackathon(getHackathonById(teamRequest.hackathonId()));
            teamToEdit.setTeamMembersIds(teamRequest.teamMembersIds());
            teamToEdit.setTags(teamRequest.tags());
            return TeamMapper.toResponse(teamRepository.save(teamToEdit));
        }).orElseThrow(() -> {
            log.info(String.format("Team id: %d not found", id));
            return new ResourceNotFoundException(String.format("Team " +
                    "id: %d not found", id));
        });
    }



    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonService.findById(hackathonId).orElseThrow(() -> {
            log.info("Hacathon id: {} not found", hackathonId);
            throw new ResourceNotFoundException(String.format(
                    "Hacathon id: %d not found", hackathonId));
        });
    }


    public void processInvitation(Long teamId,
            String userId, TeamInvitation teamInvitation) {

        Team team = teamRepository.findById(teamId).orElseThrow();

        TeamInvitation teamInvitation1 =
                TeamInvitation.builder()
                        // .teamId(team.getId())
                        .teamName(team.getName())
                        .invitationStatus(InvitationStatus.PENDING)
                        .fromUserId(1L)
                        .toUserId(12L)
                        .build();

       team.addUserInvitationToTeam(teamInvitation1);

       teamRepository.save(team);

       notificationService.sendTeamInviteNotification(teamInvitation1);
    }

    public void findInvitesByUserId(String id) {

        // TODO send db query where inv.status == PENDING

//        Set<TeamInvitation> x = teamRepository.findInvitationsId(id);
//
//        x.forEach(inv -> {
//            if (inv.getInvitationStatus().equals(InvitationStatus.PENDING)) {
//                notificationService.sendTeamInviteNotification(inv);
//            }
//        });
    }
}
