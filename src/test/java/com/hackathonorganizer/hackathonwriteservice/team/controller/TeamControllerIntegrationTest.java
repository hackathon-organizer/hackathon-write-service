package com.hackathonorganizer.hackathonwriteservice.team.controller;

import com.hackathonorganizer.hackathonwriteservice.BaseIntegrationTest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.creator.TestDataUtils;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamInvitationDto;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamRequest;
import com.hackathonorganizer.hackathonwriteservice.team.model.dto.TeamVisibilityStatusRequest;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamInvitationRepository;
import com.hackathonorganizer.hackathonwriteservice.team.repository.TeamRepository;
import com.hackathonorganizer.hackathonwriteservice.utils.RestCommunicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamInvitationRepository teamInvitationRepository;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private RestCommunicator restCommunicator;

    @Autowired
    private TestDataUtils testDataUtils;

    @BeforeEach
    void setUp() {
        teamInvitationRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void shouldCreateTeam() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        hackathon.setHackathonParticipantsIds(Set.of(1L));
        hackathon = testDataUtils.updateHackathonProperties(hackathon);

        testDataUtils.mockUserVerification();
        testDataUtils.mockUserExternalCall(hackathon);

        TeamRequest request =
                new TeamRequest(1L, "name", true, "desc", hackathon.getId(), List.of());

        // when

        ResultActions resultActions = mockMvc.perform(postTeamJsonRequest(request, Role.USER));

        // then
        resultActions.andExpect(status().isCreated())

                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()));
        assertThat(teamRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldEditTeamById() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        Team savedTeam = testDataUtils.createTeam(hackathon);

        testDataUtils.mockUserExternalCall(hackathon);
        testDataUtils.mockUserVerification();

        TeamRequest request = new TeamRequest(
                1L,
                "team name edited",
                true, "desc edited",
                9L,
                List.of());

        // when
        ResultActions resultActions =
                mockMvc.perform(putTeamJsonRequest(
                        request, Role.ORGANIZER, "/", String.valueOf(savedTeam.getId())));

        // then
        resultActions.andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(savedTeam.getId()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()));
        assertThat(teamRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldProcessInvitation() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);

        Long userId = 1L;

        // when

        ResultActions resultActions = mockMvc.perform(postTeamJsonRequest(
                userId,
                Role.USER,
                team.getId().toString(),
                "invites",
                "?username=username"));

        // then

        resultActions.andExpect(status().isOk());
        assertThat(teamInvitationRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateInvitationStatusAccepted() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);
        TeamInvitation invitation = teamInvitationRepository.save(new TeamInvitation(
                null,
                "fromUsername",
                5L,
                InvitationStatus.PENDING,
                team.getName(),
                team));

        TeamInvitationDto teamInvitationDto = new TeamInvitationDto(
                invitation.getId(),
                "fromUsername",
                15L,
                InvitationStatus.ACCEPTED,
                team.getName(),
                team.getId());

        // when

        ResultActions resultActions =
                mockMvc.perform(patchTeamJsonRequest(
                        teamInvitationDto,
                        Role.USER,
                        team.getId().toString(),
                        "invites"));

        // then

        resultActions.andExpect(status().isOk());

        assertThat(teamInvitationRepository.findAll().get(0).getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    @Test
    void shouldUpdateInvitationStatusRejected() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);
        TeamInvitation invitation = teamInvitationRepository.save(new TeamInvitation(
                null,
                "fromUsername",
                5L,
                InvitationStatus.PENDING,
                team.getName(),
                team));

        TeamInvitationDto teamInvitationDto = new TeamInvitationDto(
                invitation.getId(),
                "fromUsername",
                15L,
                InvitationStatus.REJECTED,
                team.getName(),
                team.getId());

        // when

        ResultActions resultActions =
                mockMvc.perform(patchTeamJsonRequest(teamInvitationDto,
                        Role.USER,
                        team.getId().toString(),
                        "invites"));

        // then

        resultActions.andExpect(status().isOk());

        assertThat(teamInvitationRepository.findAll().get(0).getInvitationStatus()).isEqualTo(InvitationStatus.REJECTED);
    }

    @Test
    void shouldAddUserToTeam() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);

        // when

        ResultActions resultActions =
                mockMvc.perform(
                        patchTeamJsonRequest(null, Role.USER, team.getId().toString(), "participants", "1"));

        // then

        resultActions.andExpect(status().isOk());
        assertThat(teamRepository.findById(team.getId()).get().getTeamMembersIds().size()).isEqualTo(1);
        assertThat(teamRepository.findById(team.getId()).get().getTeamMembersIds()).contains(1L);
    }

    @Test
    void shouldNotAddUserToTeam() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);
        team.setIsOpen(false);
        teamRepository.save(team);

        // when

        ResultActions resultActions =
                mockMvc.perform(
                        patchTeamJsonRequest(null, Role.USER, team.getId().toString(), "participants", "1"));

        // then

        resultActions.andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.message").value("Team Team name is not accepting new members"));
        assertThat(teamRepository.findById(team.getId()).get().getTeamMembersIds().size()).isEqualTo(0);
        assertThat(teamRepository.findById(team.getId()).get().getTeamMembersIds()).doesNotContain(1L);
    }

    @Test
    void shouldCloseTeamForMembers() throws Exception {
        //given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);

        TeamVisibilityStatusRequest teamVisibilityStatusRequest = new TeamVisibilityStatusRequest(1L, false);

        // when

        ResultActions resultActions =
                mockMvc.perform(patchTeamJsonRequest(teamVisibilityStatusRequest, Role.TEAM_OWNER, team.getId().toString()));

        //then

        resultActions.andExpect(status().isOk());

        assertThat(teamRepository.findById(team.getId()).get().getIsOpen()).isFalse();
    }

    @Test
    void shouldOpenTeamForMembers() throws Exception {
        //given

        Hackathon hackathon = testDataUtils.createHackathon();
        testDataUtils.mockUserVerification();
        Team team = testDataUtils.createTeam(hackathon);
        team.setIsOpen(false);
        teamRepository.save(team);

        TeamVisibilityStatusRequest teamVisibilityStatusRequest = new TeamVisibilityStatusRequest(1L, true);

        // when

        ResultActions resultActions =
                mockMvc.perform(patchTeamJsonRequest(teamVisibilityStatusRequest, Role.TEAM_OWNER, team.getId().toString()));

        //then

        resultActions.andExpect(status().isOk());

        assertThat(teamRepository.findById(team.getId()).get().getIsOpen()).isTrue();
    }

    @Test
    void shouldNotOpenTeamForMembers() throws Exception {
        //given

        Hackathon hackathon = testDataUtils.createHackathon();
        Team team = testDataUtils.createTeam(hackathon);
        team.setOwnerId(99L);
        teamRepository.save(team);

        testDataUtils.mockUserExternalCall(hackathon);

        TeamVisibilityStatusRequest teamVisibilityStatusRequest = new TeamVisibilityStatusRequest(11L, true);

        // when

        ResultActions resultActions =
                mockMvc.perform(patchTeamJsonRequest(teamVisibilityStatusRequest, Role.TEAM_OWNER, team.getId().toString()));

        //then

        resultActions.andExpect(status().isForbidden())

                .andExpect(jsonPath("$.message").value("Can't edit team because user with id 11 is not team owner"));
    }
}
