package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamVisibilityStatusRequest;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
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

            log.info("Trying to save new team {}", teamToSave.getId());
            Team savedTeam = teamRepository.save(teamToSave);
            log.info("Team with id: " + savedTeam.getId() + " saved successfully");

            createTeamChatRoom(savedTeam);
            return TeamMapper.mapToTeamDto(savedTeam);
        } else {

            log.warn("Can't create team because user with id: {} is not " +
                    "hackathon participant", teamRequest.ownerId());

            throw new TeamException("Can't create team because user with id: " +
                    teamRequest.ownerId() + " is not hackathon participant",
                    HttpStatus.NOT_FOUND);
        }
    }

    public TeamResponse editTeamById(Long id, TeamRequest teamRequest, Principal principal) {

        if (checkIfUserIsTeamOwner(teamRequest.ownerId(), id) &&
                userPermissionValidator.verifyUser(principal, teamRequest.ownerId())) {

            Team team = getTeamById(id);

            team.setName(teamRequest.name());
            team.setDescription(teamRequest.description());
            team.setTags(teamRequest.tags());

            return TeamMapper.mapToTeamDto(teamRepository.save(team));
        } else {

            log.warn("Can't edit team because user with id: {} is not " +
                    "team owner", teamRequest.ownerId());

            throw new TeamException("Can't edit team because user with id " +
                    teamRequest.ownerId() + " is not team owner",
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

    public void updateInvitationStatus(TeamInvitationDto teamInvitationDto, Principal principal) {

        Team team = getTeamById(teamInvitationDto.teamId());

        TeamInvitation teamInvitation = getTeamInvitationById(teamInvitationDto.id());

        if (teamInvitationDto.invitationStatus() == InvitationStatus.ACCEPTED) {
            teamInvitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            keycloakService.removeRoles(principal.getName());
            team.addUserToTeam(teamInvitationDto.toUserId());
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
        } else {
            log.info("Team with id: " + teamId + " is not accepting new members");

            throw new TeamException(String.format( "Team with id: %d is not accepting new members",
                    team.getId()), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public boolean openOrCloseTeamForMembers(Long teamId, TeamVisibilityStatusRequest teamVisibilityStatusRequest) {

        if (checkIfUserIsTeamOwner(teamVisibilityStatusRequest.userId(), teamId)) {

            Team team = getTeamById(teamId);

            team.setIsOpen(teamVisibilityStatusRequest.isOpen());

            Team savedTeam = teamRepository.save(team);

            log.info("Team with id: {} is now {}", savedTeam.getId(), savedTeam.getIsOpen() ? "open" : "closed");

            return savedTeam.getIsOpen();
        } else {

            log.warn("Can't edit team because user with id: {} is not team owner", teamVisibilityStatusRequest.userId());

            throw new TeamException("Can't edit team because user with id " +
                    teamVisibilityStatusRequest.userId() + " is not team owner",
                    HttpStatus.NOT_FOUND);
        }
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

    private boolean checkIfUserIsTeamOwner(Long userId, Long teamId) {

        Team team = getTeamById(teamId);

        return team.getOwnerId().equals(userId);
    }
}
