package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import com.hackathonorganizer.hackathonwriteservice.team.exception.ResourceNotFoundException;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.team.utils.TeamMapper;
import com.hackathonorganizer.hackathonwriteservice.team.utils.dto.EditUserRequestDto;
import com.hackathonorganizer.hackathonwriteservice.team.utils.dto.UserResponseDto;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {


    private final TeamRepository teamRepository;

    private final TagService tagService;

    private final HackathonService hackathonService;

    private final RestTemplate restTemplate;

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

    public void inviteUserToTeam(Long teamId, String invitedUserNickname) {

        Team team = teamRepository.findById(teamId).orElseThrow(() -> {
            log.info(String.format("Team id: %d not found", teamId));
            return new ResourceNotFoundException(String.format("Team " +
                    "id: %d not found", teamId));
        });

        UserResponseDto userResponse =
                restTemplate.getForObject("http://localhost:8199/users?username="
                                + invitedUserNickname, UserResponseDto.class);

        if (userResponse != null) {

            TeamInvitation teamInvitation =
                    new TeamInvitation(userResponse.id(),
                            InvitationStatus.PENDING, "Test team",
                            team.getId());

            Set<TeamInvitation> invitations = userResponse.invitations();
            invitations.add(teamInvitation);

            EditUserRequestDto editUserRequestDto = new EditUserRequestDto(
                    userResponse.id(),
                    invitations
            );

            ResponseEntity userAddInvitationResponse =
                    restTemplate.patchForObject("http://localhost/8199/users/" +
                                    userResponse.id(), editUserRequestDto,
                                        ResponseEntity.class);

            log.info("User id: {} send invite to team", userResponse.id());
        }
    }

    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonService.findById(hackathonId).orElseThrow(() -> {
            log.info("Hacathon id: {} not found", hackathonId);
            throw new ResourceNotFoundException(String.format(
                    "Hacathon id: %d not found", hackathonId));
        });
    }
}
