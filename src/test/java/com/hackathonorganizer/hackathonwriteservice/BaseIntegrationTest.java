package com.hackathonorganizer.hackathonwriteservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathonorganizer.hackathonwriteservice.keycloak.Role;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(classes = HackathonWriteServiceApplication.class)
@Slf4j
public class BaseIntegrationTest {

    private static final String POSTGRESQL_IMAGE_VERSION = "postgres:14.4";
    private static final String KC_IMAGE_VERSION = "quay.io/keycloak/keycloak:16.0.0";
    private static final PostgreSQLContainer sqlContainer;
    private static final KeycloakContainer keycloakContainer;

    private final String BASE_URL_HACKATHON = "/api/v1/write/hackathons/";
    private final String BASE_URL_TEAM = "/api/v1/write/teams/";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RestTemplate restTemplate;

    static {
        sqlContainer = new PostgreSQLContainer(POSTGRESQL_IMAGE_VERSION);
        sqlContainer.start();

        keycloakContainer = new KeycloakContainer(KC_IMAGE_VERSION).withRealmImportFile("/realm-export.json");
        keycloakContainer.start();
    }

    @DynamicPropertySource
    public static void overrideProperties(final DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
        registry.add("spring.datasource.username", sqlContainer::getUsername);

        registry.add("keycloak.authUrl", keycloakContainer::getAuthServerUrl);
        registry.add("keycloak.username", keycloakContainer::getAdminUsername);
        registry.add("keycloak.password", keycloakContainer::getAdminPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/hackathon-organizer");
    }

    protected String getJaneDoeBearer(Role role) {

        try {
            MultiValueMap<String, String> formData = new HttpHeaders();
            formData.put("grant_type", Collections.singletonList("password"));
            formData.put("client_id", Collections.singletonList("hackathon-organizer-client"));
            formData.put("username", Collections.singletonList("janedoe_" + role.name()));
            formData.put("password", Collections.singletonList("qwerty"));

            String result = restTemplate.postForObject(
                    keycloakContainer.getAuthServerUrl() + "/realms/hackathon-organizer/protocol/openid-connect/token", formData, String.class);

            JacksonJsonParser jsonParser = new JacksonJsonParser();

            return "Bearer " + jsonParser.parseMap(result)
                    .get("access_token")
                    .toString();
        } catch (Exception e) {
            log.error("Can't obtain an access token from Keycloak!", e);
        }

        return null;
    }

    // HACKATHON REQUESTS

    protected MockHttpServletRequestBuilder postHackathonJsonRequest(Object body, Role userRoleType,
                                                                     String... urlVariables) throws Exception {
        return post(BASE_URL_HACKATHON + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder putHackathonJsonRequest(Object body, Role userRoleType,
                                                                    String... urlVariables) throws Exception {
        return put(BASE_URL_HACKATHON + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder patchHackathonJsonRequest(Object body, Role userRoleType,
                                                                      String... urlVariables) throws Exception {
        return patch(BASE_URL_HACKATHON + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder deleteHackathonJsonRequest(Object body, Role userRoleType,
                                                                       String... urlVariables) throws Exception {
        return delete(BASE_URL_HACKATHON + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    // TEAM REQUESTS

    protected MockHttpServletRequestBuilder postTeamJsonRequest(Object body, Role userRoleType,
                                                                String... urlVariables) throws Exception {
        return post(BASE_URL_TEAM + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder putTeamJsonRequest(Object body, Role userRoleType,
                                                               String... urlVariables) throws Exception {
        return put(BASE_URL_TEAM + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder patchTeamJsonRequest(Object body, Role userRoleType,
                                                                 String... urlVariables) throws Exception {
        return patch(BASE_URL_TEAM + String.join("/", urlVariables))
                .header("Authorization", getJaneDoeBearer(userRoleType))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
}

