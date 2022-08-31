package com.hackathonorganizer.hackathonwriteservice.websocket;

import com.hackathonorganizer.hackathonwriteservice.team.service.TeamService;
import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import com.sun.security.auth.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

        String userId = httpServletRequest.getParameter("userId");

        // TODO find a better way to store userId

        NotificationService.userId = userId;

        log.info("User with ID '{}' connected to the websocket", userId);

        return new UserPrincipal(userId);
    }
}
