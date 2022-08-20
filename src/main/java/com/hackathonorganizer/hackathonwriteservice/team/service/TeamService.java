package com.hackathonorganizer.hackathonwriteservice.team.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.team.exception.ResourceNotFoundException;
import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TagRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.team.utils.TeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {


    private final TeamRepository teamRepository;

    private final TagService tagService;

    //TODO change to service when it will be ready
    private final HackathonRepository hackathonRepository;

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

        return hackathonRepository.findById(hackathonId).orElseThrow(() -> {
            log.info("Hacathon id: {} not found", hackathonId);
            throw new ResourceNotFoundException(String.format(
                    "Hacathon id: %d not found", hackathonId));
        });
    }
}
