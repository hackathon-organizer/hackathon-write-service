package com.hackathonorganizer.hackathonwriteservice.hackathon.controller;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaAnswerDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import com.hackathonorganizer.hackathonwriteservice.hackathon.service.HackathonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/write/hackathons")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @RolesAllowed({"USER"})
    public HackathonResponse createHackathon(@RequestBody @Valid HackathonRequest hackathon, Principal principal) {

        return hackathonService.createHackathon(hackathon, principal);
    }

    @PutMapping("/{hackathonId}")
    @RolesAllowed({"ORGANIZER"})
    public HackathonResponse updateHackathon(@PathVariable("hackathonId") Long hackathonId,
                                             @RequestBody @Valid HackathonRequest hackathonRequest,
                                             Principal principal) {

        return hackathonService.updateHackathon(hackathonId, hackathonRequest, principal);
    }

    @PatchMapping("/{hackathonId}/participants/{userId}")
    @RolesAllowed("USER")
    public void signUpUserToHackathon(@PathVariable("hackathonId") Long hackathonId,
                                      @PathVariable("userId") Long userId,
                                      Principal principal) {

        hackathonService.assignUserToHackathon(hackathonId, userId, principal);
    }

    @PatchMapping("/{hackathonId}/participants/{userId}/remove")
    @RolesAllowed({"ORGANIZER"})
    public void removeUserFromHackathon(@PathVariable("hackathonId") Long hackathonId,
                                        @PathVariable("userId") Long userId,
                                        Principal principal) {

        hackathonService.removeUserFromHackathonParticipants(hackathonId, userId, principal);
    }

    @PostMapping("/{hackathonId}/criteria")
    @ResponseStatus(code = HttpStatus.CREATED)
    @RolesAllowed({"ORGANIZER"})
    public List<CriteriaDto> addRateCriteriaToHackathon(@PathVariable("hackathonId") Long hackathonId,
                                                        @RequestBody List<CriteriaDto> criteriaRequest,
                                                        Principal principal) {

        return hackathonService.addRateCriteriaToHackathon(hackathonId, criteriaRequest, principal);
    }

    @PutMapping("/{hackathonId}/criteria")
    @RolesAllowed({"JURY", "ORGANIZER"})
    public void updateRateCriteriaInHackathon(@PathVariable("hackathonId") Long hackathonId,
                                              @RequestBody List<CriteriaDto> criteriaRequest,
                                              Principal principal) {

        hackathonService.updateRateCriteriaInHackathon(hackathonId, criteriaRequest, principal);
    }

    @PatchMapping("/{hackathonId}/criteria/answers")
    @RolesAllowed({"JURY", "ORGANIZER"})
    public List<CriteriaAnswerDto> saveTeamRatingAnswers(@PathVariable("hackathonId") Long hackathonId,
                                                         @RequestBody List<CriteriaAnswerDto> criteria,
                                                         Principal principal) {

        return hackathonService.saveCriteriaAnswers(hackathonId, criteria, principal);
    }

    @DeleteMapping("/{hackathonId}/criteria/{criteriaId}")
    @RolesAllowed({"ORGANIZER"})
    public void deleteCriteria(@PathVariable("hackathonId") Long hackathonId,
                               @PathVariable("criteriaId") Long criteriaId,
                               Principal principal) {

        hackathonService.deleteCriteria(hackathonId, criteriaId, principal);
    }

    @PostMapping("/{hackathonId}/files")
    @RolesAllowed({"ORGANIZER"})
    public void uploadFile(@RequestParam("file") MultipartFile file,
                           @PathVariable("hackathonId") Long hackathonId,
                           Principal principal) {

        hackathonService.uploadFile(file, hackathonId, principal);
    }
}
