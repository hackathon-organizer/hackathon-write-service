package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.TeamException;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserMembershipRequest;
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
            restTemplate.put("http://localhost:9090/api/v1/write/users/"
                    + userId + "/membership", userMembershipRequest);

            log.info("Send user membership status update");
        } catch (HttpServerErrorException.ServiceUnavailable ex) {
            log.warn("User service is unavailable. Can't update user " +
                    " membership. {}", ex.getMessage());

            throw new TeamException("User service is unavailable. Can't update user " +
                    " membership", HttpStatus.SERVICE_UNAVAILABLE);
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

}
