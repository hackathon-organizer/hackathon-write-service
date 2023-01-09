package com.hackathonorganizer.hackathonwriteservice.keycloak;

import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;

public enum Role {
    ORGANIZER,
    TEAM_OWNER,
    MENTOR,
    JURY,
    USER
}
