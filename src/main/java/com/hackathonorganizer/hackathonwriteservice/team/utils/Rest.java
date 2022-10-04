package com.hackathonorganizer.hackathonwriteservice.team.utils;

import com.hackathonorganizer.hackathonwriteservice.team.utils.dto.UserMembershipRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class Rest {

    private final RestTemplate restTemplate;

    public void updateUserHackathonId(Long userId,
            UserMembershipRequest userMembershipRequest) {

        restTemplate.put("http://localhost:9090/api/v1/write/users/"
                 + userId + "/membership", userMembershipRequest);

        log.info("Send user membership status update");
    }

}
