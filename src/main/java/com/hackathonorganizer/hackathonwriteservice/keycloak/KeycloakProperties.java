package com.hackathonorganizer.hackathonwriteservice.keycloak;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "keycloak")
@Component
@Getter
@Setter
class KeycloakProperties {

    private String authUrl;
    private String realm;
    private String masterRealm;
    private String clientId;
    private String username;
    private String password;
}
