package com.hackathonorganizer.hackathonwriteservice.keycloak;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.HackathonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final KeycloakProperties keycloakProperties;

    public Keycloak buildKeyCloak() {

        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getAuthUrl())
                .realm(keycloakProperties.getMasterRealm())
                .clientId(keycloakProperties.getClientId())
                .username(keycloakProperties.getUsername())
                .password(keycloakProperties.getPassword())
                .build();
    }

    public void updateUserRole(String keycloakId, Role newRole) {

        try {
            Keycloak keycloak = buildKeyCloak();
            String realm = "hackathon-organizer";

            UsersResource usersResource = keycloak.realm(realm).users();
            UserResource userResource = usersResource.get(keycloakId);

            List<RoleRepresentation> roles = userResource.roles().realmLevel().listEffective();
            RoleRepresentation foundedRole = userResource.roles().realmLevel().listAvailable()
                    .stream().filter(role -> role.getName().equals(newRole.name())).toList().get(0);

            roles.add(foundedRole);
            userResource.roles().realmLevel().add(roles);

            log.info("Role {} added to user: {}", newRole, keycloakId);
        } catch (Exception ex) {
            log.info("Can't update user roles");
            ex.printStackTrace();
            throw new HackathonException("Can't update user roles", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeRoles(String keycloakId) {
        try {
            Keycloak keycloak = buildKeyCloak();
            String realm = "hackathon-organizer";

            UsersResource usersResource = keycloak.realm(realm).users();
            UserResource userResource = usersResource.get(keycloakId);

            List<RoleRepresentation> defaultRoles = userResource.roles().realmLevel().listAll().stream()
                    .filter(role -> role.getName().equals("default-roles-hackathon-organizer") ||
                            role.getName().equals("USER")).toList();

            List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll();
            userResource.roles().realmLevel().remove(roles);

            userResource.roles().realmLevel().add(defaultRoles);

            log.info("Roles cleared from user: {}", keycloakId);
        } catch (Exception ex) {
            log.info("Can't remove user roles");
            ex.printStackTrace();
            throw new HackathonException("Can't remove user roles", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
