package com.hackathonorganizer.hackathonwriteservice.configuration;

import com.sun.security.auth.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String sessionId = request.getURI().getQuery().split("sessionId=")[1];

        return new UserPrincipal(sessionId);
    }
}
