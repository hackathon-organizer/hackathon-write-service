package com.hackathonorganizer.hackathonwriteservice.hackathon.service;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.HackathonException;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonResponse;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.utils.HackathonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class HackathonService {

    private final HackathonRepository hackathonRepository;

    public HackathonResponse createHackathon(HackathonRequest hackathonRequest) {

        Hackathon hackathon = Hackathon.builder()
                .name(hackathonRequest.name())
                .description(hackathonRequest.description())
                .organizerInfo(hackathonRequest.organizerInfo())
                .eventStartDate(hackathonRequest.eventStartDate())
                .eventEndDate(hackathonRequest.eventEndDate())
                .build();

        Hackathon savedHackathon = saveToRepository(hackathon);

        log.info("Hackathon with id: {} saved successfully",
                savedHackathon.getId());

        return HackathonMapper.mapToDto(savedHackathon);
    }

    public HackathonResponse updateHackathonData(Long hackathonId,
            HackathonRequest hackathonUpdatedData) {

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() ->
                        new HackathonException(String.format("Hackathon with " +
                                "id: %d not found", hackathonId),
                                HttpStatus.NOT_FOUND));

        Hackathon updatedHackathon = hackathon.toBuilder()
                .name(hackathonUpdatedData.name())
                .description(hackathonUpdatedData.description())
                .organizerInfo(hackathonUpdatedData.organizerInfo())
                .eventStartDate(hackathonUpdatedData.eventStartDate())
                .eventEndDate(hackathonUpdatedData.eventEndDate())
                .build();


        Hackathon savedHackathon = saveToRepository(updatedHackathon);

        log.info("Hackathon with id: {} updated successfully",
                savedHackathon.getId());

        return HackathonMapper.mapToDto(savedHackathon);
    }

    public void deactivateHackathon(Long hackathonId) {

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonException(String.format(
                        "Hackathon with id: %d not found", hackathonId),
                        HttpStatus.NOT_FOUND));

        hackathon.setActive(false);

        saveToRepository(hackathon);

        log.info("Hackathon with id: {} deactivated successfully", hackathonId);
    }

    public void assignUserToHackathon(Long hackathonId, Long userId) {

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonException(String.format(
                        "Hackathon with id: %d not found", hackathonId),
                        HttpStatus.NOT_FOUND));

        hackathon.addUserToHackathonParticipants(userId);

        saveToRepository(hackathon);

        log.info("User with id: {} successfully added to hackathon with id: " +
                "{}", userId, hackathonId);
    }

    public void removeUserFromHackathonParticipants(Long hackathonId,
            Long userId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonException(
                String.format("Hackathon with id: %d not found", hackathonId),
                HttpStatus.NOT_FOUND));

        hackathon.removeUserFromHackathonParticipants(userId);

        saveToRepository(hackathon);

        log.info("User with id: {} successfully removed from hackathon with " +
                "id: {}", userId, hackathonId);
    }

    public Optional<Hackathon> findById(Long id) {
        return hackathonRepository.findById(id);
    }

    private Hackathon saveToRepository(Hackathon hackathon) {
        return hackathonRepository.save(hackathon);
    }
}
