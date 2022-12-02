package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserMembershipRequest;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserMembershipResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void updateUserMembership(Long userId, UserMembershipRequest userMembershipRequest) {

        log.info("Trying update team membership to user with id: " + userId);

        try {
            restTemplate.patchForObject("http://localhost:9090/api/v1/write/users/"
                    + userId + "/membership", userMembershipRequest, Void.class);

            log.info("Send user membership status update");
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            log.warn("User service is unavailable. Can't update user " +
                    " membership. {}", ex.getMessage());

            throw new TeamException("User service is unavailable. Can't update user " +
                    "membership", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }

    public Long createTeamChatRoom(Long teamId) {

        log.info("Trying to create new chat room for team with id: " + teamId);

        try {
            ResponseEntity<Long> chatRoomId =  restTemplate.postForEntity(
                    "http://localhost:9090/api/v1/messages", teamId, Long.class);

            return chatRoomId.getBody();
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            log.warn("Messaging service is unavailable. Can't create " +
                    "team chatroom. {}", ex.getMessage());

            throw new TeamException("Messaging service is unavailable. Can't create " +
                    "team chatroom.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public UserMembershipResponse getUserMembershipDetails(Long userId) {

        log.info("Trying to fetch user with id: {} membership", userId);

        try {
            UserMembershipResponse userMembershipResponse =
                    restTemplate.getForObject("http://localhost:9090/api/v1/read/users/"
                    + userId + "/membership", UserMembershipResponse.class);

            log.info("Send user membership status update");

            return userMembershipResponse;
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            log.warn("User service is unavailable. Can't get user " +
                    " membership. {}", ex.getMessage());

            throw new TeamException("User service is unavailable. Can't get user " +
                    "membership", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

}
