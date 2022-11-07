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

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final RestCommunicator restCommunicator;
    private final CriteriaRepository criteriaRepository;

    public HackathonResponse createHackathon(HackathonRequest hackathonRequest) {

        Hackathon hackathon = Hackathon.builder()
                .name(hackathonRequest.name())
                .description(hackathonRequest.description())
                .organizerInfo(hackathonRequest.organizerInfo())
                .ownerId(hackathonRequest.ownerId())
                .eventStartDate(hackathonRequest.eventStartDate())
                .eventEndDate(hackathonRequest.eventEndDate())
                .build();

        Hackathon savedHackathon = hackathonRepository.save(hackathon);

        log.info("Hackathon with id: {} saved successfully",
                savedHackathon.getId());

        return HackathonMapper.mapToDto(savedHackathon);
    }

    public HackathonResponse updateHackathon(Long hackathonId, HackathonRequest hackathonUpdatedData) {

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

        hackathonRepository.save(hackathon);

        UserMembershipRequest userMembershipRequest =
                new UserMembershipRequest(hackathonId, 0L);

        restCommunicator.updateUserMembership(userId, userMembershipRequest);

        log.info("User with id: {} successfully added to hackathon with id: " +
                "{}", userId, hackathonId);
    }

    public void removeUserFromHackathonParticipants(Long hackathonId, Long userId) {

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonException(
                        String.format("Hackathon with id: %d not found",
                                hackathonId),
                        HttpStatus.NOT_FOUND));

        hackathon.removeUserFromHackathonParticipants(userId);

        hackathonRepository.save(hackathon);

        log.info("User with id: {} successfully removed from hackathon with " +
                "id: {}", userId, hackathonId);
    }

    public Optional<Hackathon> findById(Long id) {
        return hackathonRepository.findById(id);
    }

    public void addRateCriteriaToHackathon(Long hackathonId,
            List<CriteriaDto> criteria) {


        Hackathon hackathon =
                hackathonRepository.findById(hackathonId).orElseThrow();

        criteria.forEach(criteria1 -> {

            Criteria c = Criteria.builder()
                    .name(criteria1.name())
                    .build();

            c.setHackathon(hackathon);

            criteriaRepository.save(c);

        });

        log.info("Criteria for hackathon {} saved successfully", hackathonId);
    }

    public void updateRateCriteriaToHackathon(Long hackathonId,
            List<CriteriaDto> criteria) {

        Hackathon hackathon =
                hackathonRepository.findById(hackathonId).orElseThrow();


        criteria.forEach(criteria1 -> {

            Criteria c;

            if (criteriaRepository.existsCriteriaByNameAndHackathonId(criteria1.name(), criteria1.hackathonId())) {

               return;
            } else {

                if (criteria1.id() != null) {
                    c = criteriaRepository.findById(criteria1.id()).orElseThrow();

                    c.setName(criteria1.name().trim());
                    c.setHackathon(hackathon);
                } else {
                    c = Criteria.builder()
                            .name(criteria1.name().trim())
                            .hackathon(hackathon)
                            .build();
                }
            }

            criteriaRepository.save(c);

        });

        log.info("Criteria for hackathon {} saved successfully", hackathonId);
    }

    public void saveCriteriaAnswers(List<CriteriaAnswerRequest> criteriaAnswers) {

        criteriaAnswers.forEach(criteriaRequest -> {

            Criteria criteria =
                    criteriaRepository.findById(criteriaRequest.id()).orElseThrow();

            criteriaRequest.criteriaAnswer().setCriteria(criteria);
            criteria.addAnswer(criteriaRequest.criteriaAnswer());

            criteriaRepository.save(criteria);
        });

        log.info("Criteria answers for hackathon saved successfully");
    }

    public void deleteCriteria(Long criteriaId) {

        criteriaRepository.deleteById(criteriaId);

        log.info("Criteria was deleted successfully");
    }

    private Hackathon getHackathonById(Long hackathonId) {

        return hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonException(String.format(
                        "Hackathon with id: %d not found", hackathonId),
                        HttpStatus.NOT_FOUND));
    }
}
