package com.hackathonorganizer.hackathonwriteservice.hackathon.controller;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.*;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/write/hackathons")
@AllArgsConstructor
@Slf4j
public class HackathonController {
    private final HackathonService hackathonService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public HackathonResponse createHackathon(@RequestBody @Valid HackathonRequest hackathon) {

        log.info("Trying to create new hackathon");

        return hackathonService.createHackathon(hackathon);
    }

    @PutMapping("/{id}")
    public HackathonResponse updateHackathonInfo(@PathVariable("id") Long hackathonId,
            @RequestBody @Valid HackathonRequest hackathonRequest) {

        log.info("Trying to add update hackathon with id {}", hackathonId);

        return hackathonService.updateHackathon(hackathonId, hackathonRequest);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivateHackathon(@PathVariable("id") Long hackathonId) {

        log.info("Trying to add deactivate hackathon with id {}", hackathonId);

        hackathonService.deactivateHackathon(hackathonId);
    }

    @PatchMapping("/{id}/participants/{userId}")
    public void signUpUserToHackathon(@PathVariable("id") Long hackathonId,
            @PathVariable("userId") Long userId) {

        log.info("Trying to add user with id {} to hackathon with id {}", userId, hackathonId);

        hackathonService.assignUserToHackathon(hackathonId, userId);
    }

    @PatchMapping("/{id}/participants/{userId}/remove")
    public void removeUserFromHackathon(@PathVariable("id") Long hackathonId,
            @PathVariable("userId") Long userId) {

        log.info("Trying to remove user with id {} from hackathon with id {}", userId, hackathonId);

        hackathonService.removeUserFromHackathonParticipants(hackathonId, userId);
    }

    @PostMapping("/{id}/criteria")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addRateCriteriaToHackathon(@PathVariable("id") Long hackathonId,
            @RequestBody List<CriteriaDto> criteriaRequest) {

        log.info("Trying to save criteria to hackathon with id {}", hackathonId);

        hackathonService.addRateCriteriaToHackathon(hackathonId, criteriaRequest);
    }

    @PutMapping("/{id}/criteria")
    public void updateRateCriteriaToHackathon(@PathVariable("id") Long hackathonId,
            @RequestBody List<CriteriaDto> criteriaRequest) {

        log.info("Trying to update criteria to hackathon with id {}",
                hackathonId);

        hackathonService.updateRateCriteriaToHackathon(hackathonId, criteriaRequest);
    }

    @PatchMapping("/{id}/criteria/answers")
    public void saveTeamRatingAnswer(@PathVariable("id") Long hackathonId,
            @RequestBody List<CriteriaAnswerRequest> criteria) {

        log.info("Trying to save criteria answer to hackathon with id {}", hackathonId);

        hackathonService.saveCriteriaAnswers(criteria);
    }

    @DeleteMapping("/criteria/{id}")
    public void saveTeamRatingAnswer(@PathVariable("id") Long criteriaId) {

        log.info("Trying to delete criteria with id {}", criteriaId);

        hackathonService.deleteCriteria(criteriaId);
    }
}
