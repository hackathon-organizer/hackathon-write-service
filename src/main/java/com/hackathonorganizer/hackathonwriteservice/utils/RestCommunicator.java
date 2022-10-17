package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserMembershipRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestCommunicator {

    private final RestTemplate restTemplate;

    public void updateUserHackathonId(Long userId,
            UserMembershipRequest userMembershipRequest) {

        restTemplate.put("http://localhost:9090/api/v1/write/users/"
                 + userId + "/membership", userMembershipRequest);

        log.info("Send user membership status update");
    }

    public Long createTeamChatRoom(Long teamId) {

        log.info("Trying to create new chat room for team with id: " + teamId);

        ResponseEntity<Long> chatRoomId =  restTemplate.postForEntity(
                "http://localhost:9090/api/v1/messages", teamId, Long.class);

        return chatRoomId.getBody();
    }

}
