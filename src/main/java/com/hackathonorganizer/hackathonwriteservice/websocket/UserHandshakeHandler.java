package com.hackathonorganizer.hackathonwriteservice.websocket;

import com.hackathonorganizer.hackathonwriteservice.websocket.service.NotificationService;
import com.sun.security.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

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
