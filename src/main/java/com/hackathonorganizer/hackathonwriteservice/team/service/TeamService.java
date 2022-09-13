package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.team.exception.ResourceNotFoundException;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.team.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {


    private final TeamRepository teamRepository;
    private final TeamInvitationRepository teamInvitationRepository;

    private final TagService tagService;

    private final HackathonService hackathonService;

    private final NotificationService notificationService;

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
            return new ResourceNotFoundException(String.format("Team with id: %d not found", id));
        });
    }

    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonService.findById(hackathonId).orElseThrow(() -> {
            log.info("Hackathon with id: {} not found", hackathonId);
            throw new ResourceNotFoundException(String.format(
                    "Hackathon with id: %d not found", hackathonId));
        });
    }

    public void processInvitation(Long teamId, Long userId, String fromUserUsername) {

        Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Team with id: %d not found", teamId)));

        TeamInvitation teamInvitation = TeamInvitation.builder()
                        .teamName(team.getName())
                        .invitationStatus(InvitationStatus.PENDING)
                        .fromUserName(fromUserUsername)
                        .toUserId(userId)
                        .team(team)
                        .build();

       TeamInvitation savedInvite = this.teamInvitationRepository.save(teamInvitation);

       team.addUserInvitationToTeam(savedInvite);

       teamRepository.save(team);

       log.info("Invitation with id: {} saved successfully", savedInvite.getId());

       TeamInvitationDto inviteDto = TeamMapper.mapToTeamInvitationDto(savedInvite);

       notificationService.sendTeamInviteNotification(inviteDto);
    }

    public void updateInvitationStatus(TeamInvitationDto teamInvitationDto) {

        Team team = teamRepository.findById(teamInvitationDto.teamId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Team with id: %d not found", teamInvitationDto.teamId())));

        TeamInvitation teamInvitation = teamInvitationRepository.findById(teamInvitationDto.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Invitation with id: %d not " + "found", teamInvitationDto.id())));

        if (teamInvitationDto.invitationStatus() == InvitationStatus.ACCEPTED) {
            teamInvitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            team.addUserToTeam(teamInvitationDto.toUserId());

            log.info("User {} added to team", teamInvitationDto.fromUserName());
        } else {
            teamInvitation.setInvitationStatus(InvitationStatus.REJECTED);
        }

        teamRepository.save(team);
        teamInvitationRepository.save(teamInvitation);

        log.info("Invitation with id: {} status updated", teamInvitation.getId());
    }
}
