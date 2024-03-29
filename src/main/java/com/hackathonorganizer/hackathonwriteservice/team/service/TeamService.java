package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamVisibilityStatusRequest;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.utils.UserPermissionValidator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final HackathonService hackathonService;
    private final NotificationService notificationService;
    private final RestCommunicator restCommunicator;
    private final KeycloakService keycloakService;
    private final UserPermissionValidator userPermissionValidator;

    public TeamResponse createTeam(TeamRequest teamRequest, Principal principal) {

        Hackathon hackathon = getHackathonById(teamRequest.hackathonId());
        UserResponseDto userResponseDto = restCommunicator.getUserByKeycloakId(principal.getName());

        if (hackathonService.isUserHackathonParticipant(hackathon.getId(), userResponseDto.currentHackathonId())) {

            Team teamToSave = Team.builder()
                    .name(teamRequest.name())
                    .description(teamRequest.description())
                    .ownerId(teamRequest.ownerId())
                    .hackathon(hackathon)
                    .tags(teamRequest.tags())
                    .build();

            keycloakService.updateUserRole(principal.getName(), Role.TEAM_OWNER);
            Team savedTeam = teamRepository.save(teamToSave);

            log.info("Team with id: " + savedTeam.getId() + " saved successfully");

            createTeamChatRoom(savedTeam);
            return TeamMapper.mapToDto(savedTeam);
        } else {

            log.info("Can't create team because user with id: {} is not hackathon participant", teamRequest.ownerId());

            throw new TeamException("Can't create team because user with id: " + teamRequest.ownerId() +
                    " is not hackathon participant", HttpStatus.NOT_FOUND);
        }
    }

    public TeamResponse updateTeamById(Long teamId, TeamRequest teamRequest, Principal principal) {

        Team team = getTeamById(teamId);

        if (checkIfUserIsTeamOwner(teamRequest.ownerId(), team) &&
                userPermissionValidator.verifyUser(principal, teamRequest.ownerId())) {

            team.setName(teamRequest.name());
            team.setDescription(teamRequest.description());
            team.setTags(teamRequest.tags());

            Team savedTeam = teamRepository.save(team);

            log.info("Team with id: " + savedTeam.getId() + " saved successfully");

            return TeamMapper.mapToDto(savedTeam);
        } else {

            log.info("Can't edit team because user with id: {} is not team owner", teamRequest.ownerId());

            throw new TeamException("Can't edit team because user with id " + teamRequest.ownerId() + " is not team owner",
                    HttpStatus.NOT_FOUND);
        }
    }

    public void processInvitation(Long teamId, Long toUserId, String fromUserUsername) {

        Team team = getTeamById(teamId);

        if (teamInvitationAlreadyExists(toUserId, team.getId())) {
            log.info("Invitation to team with id: {} already exists", team.getId());
            return;
        }

        TeamInvitation savedTeamInvitation = createTeamInvitation(team, fromUserUsername, toUserId);
        team.addUserInvitationToTeam(savedTeamInvitation);

        Team savedTeam = teamRepository.save(team);

        log.info("Team with id: {} invitation info updated successfully", savedTeam.getId());

        notificationService.sendTeamInviteNotification(savedTeamInvitation);
    }

    public void updateInvitationStatus(TeamInvitationRequest teamInvitationRequest, Principal principal) {

        Team team = getTeamById(teamInvitationRequest.teamId());
        TeamInvitation teamInvitation = getTeamInvitationById(teamInvitationRequest.id());

        if (teamInvitationRequest.invitationStatus() == InvitationStatus.ACCEPTED) {

            teamInvitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            keycloakService.removeRole(principal.getName(), Role.TEAM_OWNER);
            team.addUserToTeam(teamInvitationRequest.toUserId());
            teamRepository.save(team);
        } else {
            teamInvitation.setInvitationStatus(InvitationStatus.REJECTED);
        }

        TeamInvitation savedInvitation = teamInvitationRepository.save(teamInvitation);

        log.info("Invitation with id: {} status updated", savedInvitation.getId());
    }

    public void addUserToTeam(Long teamId, Long userId, Principal principal) {

        Team team = getTeamById(teamId);

        if (team.getIsOpen()) {
            keycloakService.removeRole(principal.getName(), Role.TEAM_OWNER);
            team.addUserToTeam(userId);

            teamRepository.save(team);
        } else {
            log.info("Team with id: {} is not accepting new members", teamId);

            throw new TeamException(String.format("Team %s is not accepting new members",
                    team.getName()), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public boolean openOrCloseTeamForMembers(Long teamId, TeamVisibilityStatusRequest teamVisibilityStatusRequest) {

        Team team = getTeamById(teamId);

        if (checkIfUserIsTeamOwner(teamVisibilityStatusRequest.userId(), team)) {

            team.setIsOpen(teamVisibilityStatusRequest.isOpen());

            Team savedTeam = teamRepository.save(team);

            log.info("Team with id: {} is now {}", savedTeam.getId(), savedTeam.getIsOpen() ? "open" : "closed");

            return savedTeam.getIsOpen();
        } else {

            log.info("Can't edit team because user with id: {} is not team owner", teamVisibilityStatusRequest.userId());

            throw new TeamException(String.format("Can't edit team because user with id %d is not team owner",
                    teamVisibilityStatusRequest.userId()), HttpStatus.FORBIDDEN);
        }
    }

    private TeamInvitation createTeamInvitation(Team team, String fromUserUsername, Long toUserId) {

        TeamInvitation teamInvitation = TeamInvitation.builder()
                .teamName(team.getName())
                .invitationStatus(InvitationStatus.PENDING)
                .fromUserName(fromUserUsername)
                .toUserId(toUserId)
                .team(team)
                .build();

        TeamInvitation savedInvite = teamInvitationRepository.save(teamInvitation);
        log.info("Invitation with id: {} saved successfully", savedInvite.getId());

        return savedInvite;
    }

    private void createTeamChatRoom(Team team) {

        team.setChatRoomId(team.getId());

        Team savedTeam = teamRepository.save(team);

        log.info("Successfully created team chatroom to team with id: {} ", savedTeam);
    }

    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonService.getHackathonById(hackathonId);
    }

    private Team getTeamById(Long teamId) {

        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(String.format("Team with id: %d not found", teamId),
                        HttpStatus.NOT_FOUND));
    }

    private TeamInvitation getTeamInvitationById(Long invitationId) {

        return teamInvitationRepository.findById(invitationId).orElseThrow(() -> new TeamException(
                String.format("Invitation with id: %d not " + "found", invitationId), HttpStatus.NOT_FOUND));
    }

    private boolean teamInvitationAlreadyExists(Long toUserId, Long teamId) {

        return teamInvitationRepository.existsByToUserIdAndTeamIdAndInvitationStatus(toUserId, teamId, InvitationStatus.PENDING);
    }

    private boolean checkIfUserIsTeamOwner(Long userId, Team team) {

        return team.getOwnerId().equals(userId);
    }
}
