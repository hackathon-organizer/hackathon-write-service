package com.hackathonorganizer.hackathonwriteservice.keycloak;

import com.hackathonorganizer.hackathonwriteservice.hackathon.exception.HackathonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
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
            String realmName = "hackathon-organizer";

            RealmResource realmResource = buildKeyCloak().realm(realmName);

            RolesResource realmRoles = realmResource.roles();
            RoleRepresentation userNewRole = realmRoles.list().stream().filter(role -> role.getName().equals(newRole.name())).findFirst()
                    .orElseThrow(() -> new HackathonException("Role " + newRole + " not found", HttpStatus.NOT_FOUND));

            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakId);
            RoleMappingResource roleMappingResource = userResource.roles();

            RoleScopeResource roleScopeResource = roleMappingResource.realmLevel();
            List<RoleRepresentation> rolesRepresentation = roleScopeResource.listAll();

            rolesRepresentation.add(userNewRole);
            userResource.roles().realmLevel().add(rolesRepresentation);

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
