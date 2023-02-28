package com.hackathonorganizer.hackathonwriteservice.hackathon.creator;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import com.hackathonorganizer.hackathonwriteservice.utils.UserPermissionValidator;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Component
@RequiredArgsConstructor
public class TestDataUtils {


    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final CriteriaRepository criteriaRepository;

    @MockBean
    private UserPermissionValidator userPermissionValidator;

    @MockBean
    private static RestCommunicator restCommunicator;

    public static Hackathon buildHackathonMock() {
        return Hackathon.builder()
                .id(5L)
                .name("Hackathon")
                .description("Desc")
                .organizerInfo("Org Info")
                .isActive(true)
                .eventStartDate(LocalDateTime.of(2522, 12, 12, 13, 0))
                .eventEndDate(LocalDateTime.of(2522, 12, 13, 13, 0))
                .ownerId(1L)
                .teams(List.of())
                .build();
    }

    public static HackathonRequest buildHackathonRequest() {
        String name = "Hackathon";
        String desc = "Hackathon desc";
        String organizerInfo = "Organizer info";
        LocalDateTime eventStartDate = LocalDateTime.now().plusDays(4);
        LocalDateTime eventEndDate = LocalDateTime.now().plusDays(7);

        return new HackathonRequest(name, desc, organizerInfo, true, eventStartDate, eventEndDate, 1L);
    }

    public static Team buildTeamMock() {
        return Team.builder()
                .id(5L)
                .name("Team")
                .description("Desc")
                .isOpen(true)
                .chatRoomId(5L)
                .hackathon(buildHackathonMock())
                .ownerId(1L)
                .tags(List.of())
                .build();
    }

    public static TeamRequest buildTeamRequest() {
        return new TeamRequest(
                1L,
                "name",
                true,
                "description",
                buildHackathonMock().getId(),
                List.of(new Tag(1L, "name")));
    }

    public static TeamInvitation buildTeamInvitationMock() {
        return new TeamInvitation(
                98L,
                "fromUsername",
                15L,
                InvitationStatus.PENDING,
                "team name",
                buildTeamMock()
        );
    }

    public Hackathon createHackathon() {

        String name = "Hackathon";
        String description = "Hackathon desc";
        String organizerInfo = "Organizer info";

        LocalDateTime eventStartDate = LocalDateTime.now().plusDays(4);
        LocalDateTime eventEndDate = LocalDateTime.now().plusDays(6);

        return hackathonRepository.save(
                Hackathon.builder()
                        .name(name)
                        .description(description)
                        .organizerInfo(organizerInfo)
                        .eventStartDate(eventStartDate)
                        .eventEndDate(eventEndDate)
                        .hackathonParticipantsIds(Set.of())
                        .ownerId(1L)
                        .build());
    }

    public Hackathon updateHackathonProperties(Hackathon hackathon) {
        return hackathonRepository.save(hackathon);
    }

    public Team createTeam(Hackathon hackathon) {

        String name = "Team name";
        String description = "Team desc";

        return teamRepository.save(
                Team.builder()
                        .name(name)
                        .description(description)
                        .hackathon(hackathon)
                        .tags(List.of())
                        .invitations(Set.of())
                        .chatRoomId(1L)
                        .isOpen(true)
                        .ownerId(1L)
                        .build()
        );
    }

    public Criteria createCriteria(Hackathon hackathon) {

        return criteriaRepository.save(
                Criteria.builder()
                        .name("crit1")
                        .hackathon(hackathon)
                        .criteriaAnswers(Set.of())
                        .build()
        );
    }

    public void mockUserVerification() {
        when(userPermissionValidator.verifyUser(any(Principal.class), anyLong())).thenReturn(true);
    }

    public void mockUserExternalCall(Hackathon hackathon) {
        when(restCommunicator.getUserByKeycloakId(anyString())).thenReturn(new UserResponseDto(
                1L,
                "user",
                "desc",
                "id",
                hackathon.getId(),
                5L,
                Set.of()));
    }
}
