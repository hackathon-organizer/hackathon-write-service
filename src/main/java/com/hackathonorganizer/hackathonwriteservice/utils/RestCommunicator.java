package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestCommunicator {

    private final RestTemplate restTemplate;

    @Value("${api-gateway.url}")
    private String gatewayUrl;

    public UserResponseDto getUserByKeycloakId(String keycloakId) {
        log.info("Trying to get details of user with id: {}", keycloakId);

        try {
            ResponseEntity<UserResponseDto> userDetails = restTemplate.getForEntity(
                    gatewayUrl + "/api/v1/read/users/keycloak/" + keycloakId, UserResponseDto.class);

            return userDetails.getBody();
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            log.warn("User service is unavailable. Can't get user details. {}", ex.getMessage());

            throw new TeamException("User service is unavailable. Can't get user details.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
