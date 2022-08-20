package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamResponse;
import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@RequestBody TeamRequest teamRequest) {
        log.info("Processing new team create request {}", teamRequest);
        return teamService.create(teamRequest);
    }

    @PutMapping("/{id}")
    public TeamResponse edit(@PathVariable Long id,
            @RequestBody TeamRequest teamRequest) {
        log.info("Processing new team id: {} edit request {}", id,
                teamRequest);
        return teamService.editById(id, teamRequest);
    }
}
