package com.hackathonorganizer.hackathonwriteservice.hackathon.controller;

import com.hackathonorganizer.hackathonwriteservice.BaseIntegrationTest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.creator.HackathonCreator;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import com.hackathonorganizer.hackathonwriteservice.hackathon.model.dto.HackathonRequest;
import com.hackathonorganizer.hackathonwriteservice.hackathon.repository.HackathonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class HackathonControllerIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private HackathonCreator hackathonCreator;

    @BeforeEach
    void setUp() {
        hackathonRepository.deleteAll();
    }

    @Test
    void shouldCreateNewHackathon() throws Exception {
        // given
        String url = "/api/v1/write/hackathons";

        HackathonRequest request = buildHackathonRequest();

        // when
        ResultActions resultActions = mockMvc.perform(postJsonRequest(url,
                request));

        // then
        resultActions.andExpect(status().isCreated());

        resultActions.andExpect(jsonPath("$.name").value(request.name()));
        resultActions.andExpect(jsonPath("$.description").value(request.description()));
    }

    @Test
    void shouldEditHackathon() throws Exception {
        // given
        String url = "/api/v1/write/hackathons/";
        Long id = 1L;
        String name = "Hackathon edited";
        String desc = "Hackathon desc edited";
        String organizerInfo = "Organizer info";
        LocalDateTime eventStartDate = LocalDateTime.now();
        LocalDateTime eventEndDate = LocalDateTime.now().plusDays(1);

        HackathonRequest request = new HackathonRequest(name, desc,
                organizerInfo, true, eventStartDate, eventEndDate, 1L);

        Hackathon savedHackathon = hackathonCreator.createHackathon();

        // when
        ResultActions resultActions =
                mockMvc.perform(putJsonRequest(url,
                        request, "/", String.valueOf(savedHackathon.getId())));

        // then
        resultActions.andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.id").value(savedHackathon.getId()));
        resultActions.andExpect(jsonPath("$.name").value(name));
        resultActions.andExpect(jsonPath("$.description").value(desc));
    }

    @Test
    void shouldThrowHackathonNotFound() throws Exception {
        // given
        String url = "/api/v1/write/hackathons/";

        HackathonRequest request = buildHackathonRequest();

        // when
        ResultActions resultActions =
                mockMvc.perform(putJsonRequest(url,
                        request, "999"));

        // then
        resultActions.andExpect(status().isNotFound());

        resultActions.andExpect(jsonPath("$.message").value("Hackathon with " +
                "id: 999 not found"));
    }


    @Test
    void shouldDeactivateHackathon() throws Exception {
        // given
        String url = "/api/v1/write/hackathons/";

        Hackathon savedHackathon = hackathonCreator.createHackathon();

        // when
        ResultActions resultActions = mockMvc.perform(patchJsonRequest(url,
                null, String.valueOf(savedHackathon.getId()), "deactivate"));

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void shouldAddUserToHackathonParticipants() throws Exception {
        // given
        String url = "/api/v1/write/hackathons/";
        String participantId = "9";

        Hackathon savedHackathon = hackathonCreator.createHackathon();



        // when
        ResultActions resultActions =
                mockMvc.perform(patchJsonRequest(url,
                        null, String.valueOf(savedHackathon.getId()),
                        "participants", participantId));

        //mockMvc.perform(putJsonRequest("/api/v1/write/users/9/membership",null));

        // then
        resultActions.andExpect(status().isOk());
    }


    @Test
    void shouldRemoveUserFromHackathonParticipants() throws Exception {
        // given
        String url = "/api/v1/write/hackathons/";
        String participantId = "9";

        Hackathon savedHackathon = hackathonCreator.createHackathon();

        // when
        ResultActions resultActions =
                mockMvc.perform(patchJsonRequest(url,
                        null, String.valueOf(savedHackathon.getId()),
                        "participants", participantId, "remove"));

        // then
        resultActions.andExpect(status().isOk());
    }

    private HackathonRequest buildHackathonRequest() {
        String name = "Hackathon";
        String desc = "Hackathon desc";
        String organizerInfo = "Organizer info";
        LocalDateTime eventStartDate = LocalDateTime.of(2022, 12, 12, 13, 0);
        LocalDateTime eventEndDate = LocalDateTime.of(2022, 12, 13, 13, 0);

        return new HackathonRequest(name, desc,
                organizerInfo, true, eventStartDate, eventEndDate, 1L);
    }

}
