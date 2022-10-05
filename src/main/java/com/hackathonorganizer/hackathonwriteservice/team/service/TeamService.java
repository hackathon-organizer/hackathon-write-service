package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.team.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.utils.Rest;
import com.hackathonorganizer.hackathonwriteservice.team.utils.dto.UserMembershipRequest;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
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
    private final Rest rest;

    public TeamResponse create(TeamRequest teamRequest) {

        Hackathon hackathon = getHackathonById(teamRequest.hackathonId());

        System.out.println(hackathon.getName());

        val teamToSave = Team.builder()
                .name(teamRequest.name())
                .description(teamRequest.description())
                .ownerId(teamRequest.ownerId())
                .hackathon(hackathon)
                .tags(teamRequest.tags())
                .build();
        log.info("Trying to save new team {}", teamToSave);

        Team savedTeam = teamRepository.save(teamToSave);

        UserMembershipRequest userHackathonMembershipRequest =
                new UserMembershipRequest(teamRequest.hackathonId(), savedTeam.getId());

        rest.updateUserHackathonId(savedTeam.getId(), userHackathonMembershipRequest);

        return TeamMapper.mapToTeamDto(savedTeam);
    }

    public TeamResponse editById(Long id, TeamRequest teamRequest) {

        return teamRepository.findById(id).map(teamToEdit -> {
            teamToEdit.setOwnerId(teamRequest.ownerId());
            teamToEdit.setHackathon(getHackathonById(teamRequest.hackathonId()));
//            teamToEdit.setTeamMembersIds(teamRequest.teamMembersIds());
            teamToEdit.setTags(teamRequest.tags());
            return TeamMapper.mapToTeamDto(teamRepository.save(teamToEdit));
        }).orElseThrow(() -> {
            log.info(String.format("Team id: %d not found", id));
            return new TeamException(String.format("Team with id: %d not " +
                    "found", id), HttpStatus.NOT_FOUND);
        });
    }

    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonService.findById(hackathonId).orElseThrow(() -> {
            log.info("Hackathon with id: {} not found", hackathonId);
            throw new TeamException(String.format(
                    "Hackathon with id: %d not found", hackathonId),
                    HttpStatus.NOT_FOUND);
        });
    }

    public void processInvitation(Long teamId, Long userId, String fromUserUsername) {

        Team team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new TeamException(
                                String.format("Team with id: %d not found", teamId),
                                HttpStatus.NOT_FOUND));

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
                        .orElseThrow(() -> new TeamException(
                                String.format("Team with id: %d not found",
                                        teamInvitationDto.teamId()),
                                HttpStatus.NOT_FOUND));

        TeamInvitation teamInvitation = teamInvitationRepository.findById(teamInvitationDto.id())
                .orElseThrow(() -> new TeamException(
                        String.format("Invitation with id: %d not " + "found",
                                teamInvitationDto.id()), HttpStatus.NOT_FOUND));

        if (teamInvitationDto.invitationStatus() == InvitationStatus.ACCEPTED) {
            teamInvitation.setInvitationStatus(InvitationStatus.ACCEPTED);
            team.addUserToTeam(teamInvitationDto.toUserId());

            UserMembershipRequest userHackathonMembershipRequest =
                    new UserMembershipRequest(team.getHackathon().getId(),
                            team.getId());

            rest.updateUserHackathonId(team.getId(),
                    userHackathonMembershipRequest);

            log.info("User {} added to team", teamInvitationDto.fromUserName());
        } else {
            teamInvitation.setInvitationStatus(InvitationStatus.REJECTED);
        }

        teamRepository.save(team);
        teamInvitationRepository.save(teamInvitation);

        log.info("Invitation with id: {} status updated", teamInvitation.getId());
    }

    public void addUserToTeam(Long teamId, Long userId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(
                        String.format("Team with id: %d not found", teamId),
                        HttpStatus.NOT_FOUND));

        if (team.getIsOpen()) {

            team.addUserToTeam(userId);

            teamRepository.save(team);

            log.info("User with id: {} added to team with id: {}", userId, teamId);
        } else {

            log.info("Team with id: " + teamId + " is not accepting new members");
            throw new TeamException("Team with id: " + teamId + " is not " +
                    "accepting new members", HttpStatus.CONFLICT);
        }
    }
}
