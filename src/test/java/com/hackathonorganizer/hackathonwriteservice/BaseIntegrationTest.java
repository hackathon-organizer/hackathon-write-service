package com.hackathonorganizer.hackathonwriteservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathonorganizer.hackathonwriteservice.HackathonWriteServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(classes = HackathonWriteServiceApplication.class)
public class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final String IMAGE_VERSION = "postgres:14.4";
    private static final PostgreSQLContainer container;

    static {
        container = new PostgreSQLContainer(IMAGE_VERSION);

        container.start();
    }

    @DynamicPropertySource
    public static void overrideProperties(final DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.datasource.username", container::getUsername);
    }

    protected MockHttpServletRequestBuilder postJsonRequest(String url,
            Object body, String... urlVariables) throws Exception {
        return post(url + String.join("/", urlVariables))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder putJsonRequest(String url,
            Object body, String... urlVariables) throws Exception {
        return put(url + String.join("/", urlVariables))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder patchJsonRequest(String url,
            Object body, String... urlVariables) throws Exception {
        return patch(url + String.join("/", urlVariables))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }
}

