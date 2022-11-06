package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserMembershipRequest;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final HackathonService hackathonService;
    private final NotificationService notificationService;
    private final RestCommunicator restCommunicator;

    public TeamResponse create(TeamRequest teamRequest) {

        Hackathon hackathon = getHackathonById(teamRequest.hackathonId());

        Team teamToSave = Team.builder()
                .name(teamRequest.name())
                .description(teamRequest.description())
                .ownerId(teamRequest.ownerId())
                .hackathon(hackathon)
                .tags(teamRequest.tags())
                .build();

        log.info("Trying to save new team {}", teamToSave.getId());

        Team savedTeam = teamRepository.save(teamToSave);

        log.info("Team with id: " + savedTeam.getId() + " saved successfully");

        updateUserTeamMembership(teamRequest.hackathonId(), teamRequest.ownerId(), savedTeam);

        return TeamMapper.mapToTeamDto(savedTeam);
    }

    public TeamResponse editById(Long id, TeamRequest teamRequest) {

        Team team = getTeamById(id);

        team.setName(teamRequest.name());
        team.setOwnerId(teamRequest.ownerId());
        team.setHackathon(getHackathonById(teamRequest.hackathonId()));
        team.setIsOpen(teamRequest.isOpen());
        team.setDescription(teamRequest.description());
        team.setTags(teamRequest.tags());

        return TeamMapper.mapToTeamDto(teamRepository.save(team));
    }

    public void processInvitation(Long teamId, Long userId, String fromUserUsername) throws InterruptedException {

        Team team = getTeamById(teamId);

        TeamInvitation teamInvitation = TeamInvitation.builder()
                        .teamName(team.getName())
                        .invitationStatus(InvitationStatus.PENDING)
                        .fromUserName(fromUserUsername)
                        .toUserId(userId)
                        .team(team).build();

       TeamInvitation savedInvite = teamInvitationRepository.save(teamInvitation);
       log.info("Invitation with id: {} saved successfully", savedInvite.getId());

       team.addUserInvitationToTeam(savedInvite);

       Team savedTeam = teamRepository.save(team);
       log.info("Team with id: {} invitation info updated successfully", savedTeam.getId());

       notificationService.sendTeamInviteNotification(savedInvite);
    }

    public void updateInvitationStatus(TeamInvitationDto teamInvitationDto) {

        Team team = getTeamById(teamInvitationDto.teamId());

        TeamInvitation teamInvitation = getTeamInvitationById(teamInvitationDto.id());

        if (teamInvitationDto.invitationStatus() == InvitationStatus.ACCEPTED) {
            teamInvitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            team.addUserToTeam(teamInvitationDto.toUserId());

            updateUserTeamMembership(team.getHackathon().getId(), teamInvitation.getToUserId(), team);
        } else {
            teamInvitation.setInvitationStatus(InvitationStatus.REJECTED);

            teamRepository.save(team);
        }

        teamInvitationRepository.save(teamInvitation);

        log.info("Invitation with id: {} status updated", teamInvitation.getId());
    }

    public void addUserToTeam(Long teamId, Long userId) {

        Team team = getTeamById(teamId);

        if (team.getIsOpen()) {

            team.addUserToTeam(userId);

            updateUserTeamMembership(team.getHackathon().getId(), userId, team);
        } else {

            log.info("Team with id: " + teamId + " is not accepting new members");

            throw new TeamException("Team with id: " + teamId + " is not " +
                    "accepting new members", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public boolean openOrCloseTeamForMembers(Long id, boolean isOpen) {

        Team team = getTeamById(id);

        team.setIsOpen(isOpen);

        Team savedTeam = teamRepository.save(team);

        log.info("Team with id: {} is now {}", savedTeam.getId(),
                savedTeam.getIsOpen() ? "open" : "closed");

        return savedTeam.getIsOpen();
    }

    private void updateUserTeamMembership(Long hackathonId, Long userId, Team team) {

        try {
            UserMembershipRequest userHackathonMembershipRequest =
                    new UserMembershipRequest(hackathonId, team.getId());

            restCommunicator.updateUserMembership(userId, userHackathonMembershipRequest);

            Long chatId = restCommunicator.createTeamChatRoom(team.getId());

            team.setChatRoomId(chatId);

            Team savedTeam = teamRepository.save(team);

            log.info("User with id: {} membership in team with id: {} was " +
                            "updated successfully", userId, savedTeam);
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            log.warn("Messaging service is unavailable. Can't update user " +
                    "team membership. {}", ex.getMessage());

            throw new TeamException("Messaging service is unavailable. Can't update user " +
                    "team membership", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonService.findById(hackathonId).orElseThrow(() ->
            new TeamException(
                    String.format("Hackathon with id: %d not found", hackathonId),
                    HttpStatus.NOT_FOUND));
    }

    private Team getTeamById(Long teamId) {

        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(
                        String.format("Team with id: %d not found", teamId),
                        HttpStatus.NOT_FOUND));
    }

    private TeamInvitation getTeamInvitationById(Long invitationId) {

       return teamInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new TeamException(
                        String.format("Invitation with id: %d not " + "found",
                        invitationId), HttpStatus.NOT_FOUND));
    }
}
