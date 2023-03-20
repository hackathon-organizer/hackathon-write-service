package com.hackathonorganizer.hackathonwriteservice.hackathon.service;

import com.hackathonorganizer.hackathonwriteservice.exception.HackathonException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.CriteriaAnswer;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.*;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaAnswerRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import com.hackathonorganizer.hackathonwriteservice.utils.HackathonMapper;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.UserPermissionValidator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import liquibase.pro.packaged.F;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final RestCommunicator restCommunicator;
    private final CriteriaRepository criteriaRepository;
    private final CriteriaAnswerRepository criteriaAnswerRepository;
    private final KeycloakService keycloakService;
    private final UserPermissionValidator userPermissionValidator;

    public HackathonResponse createHackathon(HackathonRequest hackathonRequest, Principal principal) {

        if (areEventDatesNotValid(hackathonRequest)) {
            log.info("Hackathon request contains incorrect event dates");

            throw new HackathonException("Hackathon request contains incorrect event dates", HttpStatus.BAD_REQUEST);
        }

        Hackathon hackathon = Hackathon.builder()
                .name(hackathonRequest.name())
                .description(hackathonRequest.description())
                .organizerInfo(hackathonRequest.organizerInfo())
                .ownerId(hackathonRequest.ownerId())
                .eventStartDate(hackathonRequest.eventStartDate())
                .eventEndDate(hackathonRequest.eventEndDate())
                .build();

        hackathon.addUserToHackathonParticipants(hackathonRequest.ownerId());

        keycloakService.updateUserRole(principal.getName(), Role.ORGANIZER);

        Hackathon savedHackathon = hackathonRepository.save(hackathon);

        log.info("Hackathon with id: {} saved successfully", savedHackathon.getId());

        return HackathonMapper.mapToDto(savedHackathon);
    }

    public HackathonResponse updateHackathon(Long hackathonId, HackathonRequest hackathonUpdatedData, Principal principal) {

        if (userPermissionValidator.verifyUser(principal, hackathonUpdatedData.ownerId())) {

            if (areEventDatesNotValid(hackathonUpdatedData)) {
                log.info("Hackathon request contains incorrect event dates");

                throw new HackathonException("Hackathon request contains incorrect event dates", HttpStatus.BAD_REQUEST);
            }

            Hackathon hackathon = getHackathonById(hackathonId);

            Hackathon updatedHackathon = hackathon.toBuilder()
                    .name(hackathonUpdatedData.name())
                    .description(hackathonUpdatedData.description())
                    .organizerInfo(hackathonUpdatedData.organizerInfo())
                    .ownerId(hackathonUpdatedData.ownerId())
                    .isActive(hackathonUpdatedData.isActive())
                    .eventStartDate(hackathonUpdatedData.eventStartDate())
                    .eventEndDate(hackathonUpdatedData.eventEndDate())
                    .build();

            Hackathon savedHackathon = hackathonRepository.save(updatedHackathon);

            log.info("Hackathon with id: {} updated successfully", savedHackathon.getId());

            return HackathonMapper.mapToDto(savedHackathon);
        } else {
            throw new HackathonException("User is not hackathon owner", HttpStatus.FORBIDDEN);
        }
    }

    public void deactivateHackathon(Long hackathonId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

            hackathon.setActive(false);

            hackathonRepository.save(hackathon);

            log.info("Hackathon with id: {} deactivated successfully", hackathonId);
        }
    }

    public void assignUserToHackathon(Long hackathonId, Long userId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (OffsetDateTime.now().isBefore(hackathon.getEventStartDate())) {

            hackathon.addUserToHackathonParticipants(userId);
            keycloakService.removeRoles(principal.getName());

            hackathonRepository.save(hackathon);

            log.info("User {} membership updated", userId);
        } else {
            log.info("Hackathon {} sign up already ended", hackathon.getId());

            throw new HackathonException("Hackathon " + hackathon.getName() + " sign up already ended",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void removeUserFromHackathonParticipants(Long hackathonId, Long userId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

            hackathon.removeUserFromHackathonParticipants(userId);

            hackathonRepository.save(hackathon);
        }
    }

    public List<CriteriaDto> addRateCriteriaToHackathon(Long hackathonId, List<CriteriaDto> criteria, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

            return criteria.stream().map(criterion -> {

                Criteria c = Criteria.builder()
                        .name(criterion.name())
                        .build();

                c.setHackathon(hackathon);

                return HackathonMapper.mapToCriteriaDto(criteriaRepository.save(c));
            }).toList();
        }

        return List.of();
    }

    public void updateRateCriteriaInHackathon(Long hackathonId, List<CriteriaDto> criteria, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);
        UserResponseDto userResponseDto = restCommunicator.getUserByKeycloakId(principal.getName());

        if (isUserHackathonParticipant(hackathon.getId(), userResponseDto.currentHackathonId())) {

            criteria.forEach(criterion -> {

                Criteria c;

                if (criteriaRepository.existsCriteriaByNameAndHackathonId(criterion.name(), criterion.hackathonId())) {
                    return;
                } else {

                    if (criterion.id() != null) {
                        c = criteriaRepository.findById(criterion.id()).orElseThrow(() -> new HackathonException(
                                String.format("Criteria with id: %d not found", criterion.id()),
                                HttpStatus.NOT_FOUND));

                        c.setName(criterion.name());
                        c.setHackathon(hackathon);
                    } else {
                        c = Criteria.builder()
                                .name(criterion.name())
                                .hackathon(hackathon)
                                .build();
                    }
                }

                criteriaRepository.save(c);
            });

            log.info("Criteria for hackathon {} updated successfully", hackathonId);
        } else {
            log.info("User with id: {} is not hackathon participant", userResponseDto.id());

            throw new HackathonException("User with id " + userResponseDto.id() + " is not hackathon participant",
                    HttpStatus.FORBIDDEN);
        }
    }

    public List<CriteriaAnswerDto> saveCriteriaAnswers(Long hackathonId, List<CriteriaAnswerDto> criteriaAnswers,
                                                       Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);
        UserResponseDto userResponseDto = restCommunicator.getUserByKeycloakId(principal.getName());
        List<CriteriaAnswerDto> savedAnswers = List.of();

        if (isUserHackathonParticipant(hackathon.getId(), userResponseDto.currentHackathonId())) {

            savedAnswers = criteriaAnswers.stream().map(answerRequest -> {

                Criteria criteria = criteriaRepository.findById(answerRequest.criteriaId()).orElseThrow(
                        () -> new HackathonException(
                                String.format("Criteria with id: %d not found", answerRequest.criteriaId()),
                                HttpStatus.NOT_FOUND));

                if (answerRequest.id() != null) {

                    CriteriaAnswer a = criteriaAnswerRepository.findById(answerRequest.id()).orElseThrow(
                            () -> new HackathonException(
                                    String.format("Criteria answer with id: %d not found", answerRequest.criteriaId()),
                                    HttpStatus.NOT_FOUND));

                    a.setValue(answerRequest.value());

                    return HackathonMapper.mapToCriteriaAnswerDto(criteriaAnswerRepository.save(a));
                } else {

                    CriteriaAnswer criteriaAnswer = CriteriaAnswer.builder()
                            .value(answerRequest.value())
                            .teamId(answerRequest.teamId())
                            .userId(answerRequest.userId())
                            .criteria(criteria)
                            .build();

                    return HackathonMapper.mapToCriteriaAnswerDto(criteriaAnswerRepository.save(criteriaAnswer));
                }
            }).toList();

            log.info("Criteria answers saved successfully");
            return savedAnswers;
        } else {
            log.info("User with id: {} is not hackathon participant", userResponseDto.id());

            throw new HackathonException("User with id " + userResponseDto.id() + " is not hackathon participant",
                    HttpStatus.FORBIDDEN);
        }
    }

    public void deleteCriteria(Long hackathonId, Long criteriaId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

            criteriaRepository.deleteById(criteriaId);

            log.info("Criteria was deleted successfully");
        }
    }

    private boolean areEventDatesNotValid(HackathonRequest hackathonRequest) {

        return hackathonRequest.eventStartDate().isAfter(hackathonRequest.eventEndDate()) ||
                OffsetDateTime.now().isAfter(hackathonRequest.eventStartDate()) ||
                OffsetDateTime.now().isAfter(hackathonRequest.eventEndDate());
    }

    public Hackathon getHackathonById(Long hackathonId) {

        return hackathonRepository.findById(hackathonId).orElseThrow(() -> new HackathonException(String.format(
                "Hackathon with id: %d not found", hackathonId),
                HttpStatus.NOT_FOUND));
    }

    public boolean isUserHackathonParticipant(Long hackathonId, Long userHackathonId) {
        return userHackathonId.equals(hackathonId);
    }

    public void uploadFile(MultipartFile file, Long hackathonId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

            String fileName = file.getOriginalFilename();

            try {
                Path path = Paths.get("/var/files/" + fileName);
                Files.write(path, file.getBytes());

                hackathon.setLogoName(fileName);
                hackathonRepository.save(hackathon);

                log.info("File uploaded successfully");

            } catch (Exception e) {
                log.info("Failed to upload file {}", e.getMessage());

                throw new HackathonException("Failed to upload file: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
