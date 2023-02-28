package com.hackathonorganizer.hackathonwriteservice.keycloak;

import com.hackathonorganizer.hackathonwriteservice.exception.HackathonException;
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
    private final String REALM_NAME = "hackathon-organizer";

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

        log.info("Trying to update role of user with id: {}", keycloakId);

        try {
            RealmResource realmResource = buildKeyCloak().realm(REALM_NAME);
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
        } catch (RuntimeException ex) {
            log.info("Can't update user roles");

            throw new HackathonException("Can't update user roles", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeRoles(String keycloakId) {

        log.info("Trying to remove role of user with id: {}", keycloakId);

        try {
            RealmResource realmResource = buildKeyCloak().realm(REALM_NAME);

            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakId);

            RolesResource realmRoles = realmResource.roles();
            List<RoleRepresentation> defaultRoles = realmRoles.list().stream()
                    .filter(role -> role.getName().equals("default-roles-hackathon-organizer") ||
                            role.getName().equals("USER")).toList();

            userResource.roles().realmLevel().remove(realmRoles.list());
            userResource.roles().realmLevel().add(defaultRoles);

            log.info("Roles cleared from user: {}", keycloakId);
        } catch (RuntimeException ex) {
            log.info("Can't remove user roles");

            throw new HackathonException("Can't remove user roles", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void removeRole(String keycloakId, Role roleToRemove) {
        try {
            RealmResource realmResource = buildKeyCloak().realm(REALM_NAME);

            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakId);

            RolesResource realmRoles = realmResource.roles();

            RoleRepresentation roleToRemoveRepresentation = realmRoles.get(roleToRemove.name()).toRepresentation();
            List<RoleRepresentation> defaultRoles = realmRoles.list().stream()
                    .filter(role -> role.getName().equals("default-roles-hackathon-organizer") ||
                            role.getName().equals("USER")).toList();

            userResource.roles().realmLevel().remove(List.of(roleToRemoveRepresentation));
            userResource.roles().realmLevel().add(defaultRoles);

            log.info("Roles cleared from user: {}", keycloakId);
        } catch (RuntimeException ex) {
            log.info("Can't remove role {}", roleToRemove);

            throw new HackathonException(String.format("Can't remove role %s", roleToRemove.name()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
