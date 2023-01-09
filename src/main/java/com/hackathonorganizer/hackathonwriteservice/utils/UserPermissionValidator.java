package com.hackathonorganizer.hackathonwriteservice.utils;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.HackathonException;
import com.hackathonorganizer.hackathonwriteservice.utils.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.Principal;

@AllArgsConstructor
@Component
public class UserPermissionValidator {

    private final RestCommunicator restCommunicator;

    public boolean verifyUser(Principal principal, Long userId) {

        UserResponseDto userResponseDto = restCommunicator.getUserByKeycloakId(principal.getName());

        if (userResponseDto.id().equals(userId)) {
            return true;
        } else {
            throw new HackathonException("User verification failed", HttpStatus.FORBIDDEN);
        }
    }
}
