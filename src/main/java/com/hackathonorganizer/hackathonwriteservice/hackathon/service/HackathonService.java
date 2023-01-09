package com.hackathonorganizer.hackathonwriteservice.hackathon.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.HackathonException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaAnswerRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import com.hackathonorganizer.hackathonwriteservice.utils.HackathonMapper;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.UserPermissionValidator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final RestCommunicator restCommunicator;
    private final CriteriaRepository criteriaRepository;
    private final KeycloakService keycloakService;

    private final UserPermissionValidator userPermissionValidator;

    public HackathonResponse createHackathon(HackathonRequest hackathonRequest, Principal principal) {

        if (areEventDatesNotValid(hackathonRequest)) {
            log.info("Hackathon request provides incorrect event dates");

            throw new HackathonException("Hackathon request provides incorrect event dates", HttpStatus.BAD_REQUEST);
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
                log.info("Hackathon request provides incorrect event dates");

                throw new HackathonException("Hackathon request provides incorrect event dates", HttpStatus.BAD_REQUEST);
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
        } else {
            throw new HackathonException("User is not hackathon owner", HttpStatus.FORBIDDEN);
        }
    }

    public void assignUserToHackathon(Long hackathonId, Long userId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        keycloakService.removeRoles(principal.getName());

        hackathon.addUserToHackathonParticipants(userId);

        hackathonRepository.save(hackathon);
    }

    public void removeUserFromHackathonParticipants(Long hackathonId, Long userId, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

        hackathon.removeUserFromHackathonParticipants(userId);

        hackathonRepository.save(hackathon);
        } else {
            throw new HackathonException("User is not hackathon owner", HttpStatus.FORBIDDEN);
        }
    }

    public void addRateCriteriaToHackathon(Long hackathonId, List<CriteriaDto> criteria, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);

        if (userPermissionValidator.verifyUser(principal, hackathon.getOwnerId())) {

            criteria.forEach(criterion -> {

                Criteria c = Criteria.builder()
                        .name(criterion.name())
                        .build();

                c.setHackathon(hackathon);

                criteriaRepository.save(c);
            });

            log.info("Criteria for hackathon {} saved successfully", hackathonId);
        }
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

                    c.setName(criterion.name().trim());
                    c.setHackathon(hackathon);
                } else {
                    c = Criteria.builder()
                            .name(criterion.name().trim())
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

    public void saveCriteriaAnswers(Long hackathonId, List<CriteriaAnswerRequest> criteriaAnswers, Principal principal) {

        Hackathon hackathon = getHackathonById(hackathonId);
        UserResponseDto userResponseDto = restCommunicator.getUserByKeycloakId(principal.getName());

        if (isUserHackathonParticipant(hackathon.getId(), userResponseDto.currentHackathonId())) {

            criteriaAnswers.forEach(criteriaRequest -> {

                Criteria criteria = criteriaRepository.findById(criteriaRequest.id()).
                        orElseThrow(() -> new HackathonException(
                                String.format("Criteria with id: %d not found", criteriaRequest.id()),
                                HttpStatus.NOT_FOUND));

                criteriaRequest.criteriaAnswer().setCriteria(criteria);
                criteriaRequest.criteriaAnswer().setUserId(userResponseDto.id());
                criteria.addAnswer(criteriaRequest.criteriaAnswer());

                criteriaRepository.save(criteria);
            });

            log.info("Criteria answers saved successfully");
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
                LocalDateTime.now().isAfter(hackathonRequest.eventStartDate()) ||
                LocalDateTime.now().isAfter(hackathonRequest.eventEndDate());
    }

    public Hackathon getHackathonById(Long hackathonId) {

        return hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonException(String.format(
                        "Hackathon with id: %d not found", hackathonId),
                        HttpStatus.NOT_FOUND));
    }

    public boolean isUserHackathonParticipant(Long hackathonId, Long userHackathonId) {
        return userHackathonId.equals(hackathonId);
    }
}
