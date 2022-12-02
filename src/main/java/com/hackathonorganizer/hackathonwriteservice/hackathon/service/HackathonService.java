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
import com.hackathonorganizer.hackathonwriteservice.utils.HackathonMapper;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserMembershipRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final RestCommunicator restCommunicator;
    private final CriteriaRepository criteriaRepository;

    public HackathonResponse createHackathon(HackathonRequest hackathonRequest) {

        if (areEventDatesNotValid(hackathonRequest)) {
            log.info("Hackathon request provides incorrect event dates");

            throw new HackathonException("Hackathon request provides incorrect event dates",
                    HttpStatus.BAD_REQUEST);
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

        Hackathon savedHackathon = hackathonRepository.save(hackathon);

        updateUserHackathonMembership(savedHackathon.getId(), savedHackathon.getOwnerId());

        log.info("Hackathon with id: {} saved successfully",
                savedHackathon.getId());

        return HackathonMapper.mapToDto(savedHackathon);
    }

    public HackathonResponse updateHackathon(Long hackathonId, HackathonRequest hackathonUpdatedData) {

        if (areEventDatesNotValid(hackathonUpdatedData)) {
            log.info("Hackathon request provides incorrect event dates");

            throw new HackathonException("Hackathon request provides incorrect event dates",
                    HttpStatus.BAD_REQUEST);
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
    }

    public void deactivateHackathon(Long hackathonId) {

        Hackathon hackathon = getHackathonById(hackathonId);

        hackathon.setActive(false);

        hackathonRepository.save(hackathon);

        log.info("Hackathon with id: {} deactivated successfully", hackathonId);
    }

    public void assignUserToHackathon(Long hackathonId, Long userId) {

        Hackathon hackathon = getHackathonById(hackathonId);

        hackathon.addUserToHackathonParticipants(userId);

        updateUserHackathonMembership(hackathonId, userId);

        hackathonRepository.save(hackathon);
    }

    public void removeUserFromHackathonParticipants(Long hackathonId, Long userId) {

        Hackathon hackathon = getHackathonById(hackathonId);

        hackathon.removeUserFromHackathonParticipants(userId);

        updateUserHackathonMembership(0L, userId);

        hackathonRepository.save(hackathon);
    }

    public void addRateCriteriaToHackathon(Long hackathonId, List<CriteriaDto> criteria) {

        Hackathon hackathon = getHackathonById(hackathonId);

        criteria.forEach(criterion -> {

            Criteria c = Criteria.builder()
                    .name(criterion.name())
                    .build();

            c.setHackathon(hackathon);

            criteriaRepository.save(c);
        });

        log.info("Criteria for hackathon {} saved successfully", hackathonId);
    }

    public void updateRateCriteriaToHackathon(Long hackathonId, List<CriteriaDto> criteria) {

        Hackathon hackathon = getHackathonById(hackathonId);

        criteria.forEach(criterion -> {

            Criteria c;

            if (criteriaRepository.existsCriteriaByNameAndHackathonId(criterion.name(),
                    criterion.hackathonId())) {
               return;
            } else {

                if (criterion.id() != null) {
                    c = criteriaRepository.findById(criterion.id()).orElseThrow();

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
    }

    public void saveCriteriaAnswers(List<CriteriaAnswerRequest> criteriaAnswers) {

        criteriaAnswers.forEach(criteriaRequest -> {

            Criteria criteria = criteriaRepository.findById(criteriaRequest.id()).
                    orElseThrow(() -> new HackathonException(
                            String.format("Criteria with id: %d not found", criteriaRequest.id()),
                            HttpStatus.NOT_FOUND));

            criteriaRequest.criteriaAnswer().setCriteria(criteria);
            criteria.addAnswer(criteriaRequest.criteriaAnswer());

            criteriaRepository.save(criteria);
        });

        log.info("Criteria answers saved successfully");
    }

    public void deleteCriteria(Long criteriaId) {

        criteriaRepository.deleteById(criteriaId);

        log.info("Criteria was deleted successfully");
    }

    private void updateUserHackathonMembership(Long hackathonId, Long userId) {

        UserMembershipRequest userMembershipRequest =
                new UserMembershipRequest(hackathonId, 0L);

        restCommunicator.updateUserMembership(userId, userMembershipRequest);
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

    public boolean isUserHackathonParticipant(Long hackathonId, Long userId) {

        Hackathon hackathon = getHackathonById(hackathonId);

        return hackathon.getHackathonParticipantsIds().contains(userId);
    }
}
