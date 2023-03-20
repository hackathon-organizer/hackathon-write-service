package com.hackathonorganizer.hackathonwriteservice.hackathon.controller;

import com.hackathonorganizer.hackathonwriteservice.BaseIntegrationTest;
import com.hackathonorganizer.hackathonwriteservice.creator.TestDataUtils;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaAnswerDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.CriteriaDto;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaAnswerRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.CriteriaRepository;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import com.hackathonorganizer.hackathonwriteservice.keycloak.KeycloakService;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
public class HackathonControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private CriteriaRepository criteriaRepository;

    @Autowired
    private CriteriaAnswerRepository criteriaAnswerRepository;

    @Autowired
    private TestDataUtils testDataUtils;

    @Autowired
    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() {
        criteriaAnswerRepository.deleteAll();
        criteriaRepository.deleteAll();
        hackathonRepository.deleteAll();
    }

    @Test
    void shouldCreateNewHackathon() throws Exception {
        // given

        HackathonRequest request = TestDataUtils.buildHackathonRequest();

        // when
        ResultActions resultActions = mockMvc.perform(postHackathonJsonRequest(request, Role.USER));

        // then
        resultActions.andExpect(status().isCreated())

                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()));
        assertThat(hackathonRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateHackathon() throws Exception {
        // given

        HackathonRequest request = TestDataUtils.buildHackathonRequest();

        Hackathon savedHackathon = testDataUtils.createHackathon();

        testDataUtils.mockUserVerification();

        // when
        ResultActions resultActions =
                mockMvc.perform(putHackathonJsonRequest(
                        request, Role.ORGANIZER, "/", String.valueOf(savedHackathon.getId())));

        // then
        resultActions.andExpect(status().isOk())

                .andExpect(jsonPath("$.id").value(savedHackathon.getId()))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.description").value(request.description()));
        assertThat(hackathonRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldThrowHackathonNotFound() throws Exception {
        // given

        HackathonRequest request = TestDataUtils.buildHackathonRequest();

        testDataUtils.mockUserVerification();

        // when
        ResultActions resultActions = mockMvc.perform(putHackathonJsonRequest(request, Role.ORGANIZER, "999"));

        // then
        resultActions.andExpect(status().isNotFound())

                .andExpect(jsonPath("$.message").value("Hackathon with id: 999 not found"));
    }

    @Test
    void shouldAddUserToHackathonParticipants() throws Exception {
        // given
        String url = "/api/v1/write/hackathons/";
        String participantId = "9";

        Hackathon savedHackathon = testDataUtils.createHackathon();

        // when
        ResultActions resultActions =
                mockMvc.perform(patchHackathonJsonRequest(null, Role.USER, String.valueOf(savedHackathon.getId()),
                        "participants", participantId));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void shouldAddRatingCriteriaToHackathon() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        CriteriaDto criteriaDto = new CriteriaDto(null, "crit1", hackathon.getId());
        CriteriaDto criteriaDto2 = new CriteriaDto(null, "crit2", hackathon.getId());

        List<CriteriaDto> request = List.of(criteriaDto, criteriaDto2);

        testDataUtils.mockUserVerification();

        // when
        ResultActions resultActions =
                mockMvc.perform(postHackathonJsonRequest(request, Role.ORGANIZER, hackathon.getId().toString(), "criteria"));

        // then
        resultActions.andExpect(status().isCreated())

                .andExpect(jsonPath("$.length()").value(request.size()))
                .andExpect(jsonPath("$[1].name").value(criteriaDto2.name()));
        Assertions.assertEquals(criteriaRepository.findAll().size(), 2);
    }

    @Test
    void shouldUpdateRatingCriteriaInHackathon() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        hackathon.setHackathonParticipantsIds(Set.of(1L));
        hackathon = testDataUtils.updateHackathonProperties(hackathon);
        Criteria criteria = testDataUtils.createCriteria(hackathon);
        CriteriaDto criteriaDto = new CriteriaDto(criteria.getId(), "updated crit2", hackathon.getId());

        List<CriteriaDto> criteriaRequest = List.of(criteriaDto);

        testDataUtils.mockUserExternalCall(hackathon);
        testDataUtils.mockUserVerification();

        // when
        ResultActions resultActions =
                mockMvc.perform(putHackathonJsonRequest(criteriaRequest, Role.ORGANIZER, hackathon.getId().toString(), "/criteria"));

        // then
        resultActions.andExpect(status().isOk());
        Assertions.assertEquals(criteriaRepository.findAll().size(), 1);
    }

    @Test
    void shouldThrowForbiddenWhenUpdateRatingCriteriaInHackathon() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();

        CriteriaDto criteriaDto = new CriteriaDto(null, "crit", hackathon.getId());

        List<CriteriaDto> criteriaRequest = List.of(criteriaDto);

        // when
        ResultActions resultActions =
                mockMvc.perform(putHackathonJsonRequest(criteriaRequest, Role.USER, hackathon.getId().toString(), "/criteria"));

        // then
        resultActions.andExpect(status().isForbidden());
        Assertions.assertEquals(criteriaRepository.findAll().size(), 0);
    }

    @Test
    void shouldSaveTeamRatingAnswers() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        Team team = testDataUtils.createTeam(hackathon);
        Criteria criteria = testDataUtils.createCriteria(hackathon);
        CriteriaAnswerDto criteriaDto = new CriteriaAnswerDto(null, criteria.getId(), 70, team.getId(), 1L);

        List<CriteriaAnswerDto> answers = List.of(criteriaDto);

        testDataUtils.mockUserExternalCall(hackathon);
        testDataUtils.mockUserVerification();

        // when
        ResultActions resultActions = mockMvc.perform(patchHackathonJsonRequest(answers, Role.ORGANIZER, hackathon.getId().toString(), "criteria/answers"));

        // then
        resultActions.andExpect(status().isOk())

                .andExpect(jsonPath("$.length()").value(answers.size()));
        Assertions.assertEquals(criteriaAnswerRepository.findAll().size(), 1);
    }

    @Test
    void shouldDeleteCriteria() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();
        Criteria criteria = testDataUtils.createCriteria(hackathon);

        testDataUtils.mockUserVerification();

        // when
        ResultActions resultActions =
                mockMvc.perform(deleteHackathonJsonRequest(null, Role.ORGANIZER,
                        hackathon.getId().toString(), "criteria", criteria.getId().toString()));

        // then
        resultActions.andExpect(status().isOk());
        Assertions.assertEquals(criteriaRepository.findAll().size(), 0);
    }

    @Test
    void shouldThrowForbiddenWhenDeleteCriteria() throws Exception {
        // given

        Hackathon hackathon = testDataUtils.createHackathon();

        Criteria criteria = testDataUtils.createCriteria(hackathon);

        // when
        ResultActions resultActions =
                mockMvc.perform(deleteHackathonJsonRequest(null, Role.USER,
                        hackathon.getId().toString(), "criteria", criteria.getId().toString()));

        // then
        resultActions.andExpect(status().isForbidden());
        Assertions.assertEquals(criteriaRepository.findAll().size(), 1);
    }
}
