package com.hackathonorganizer.hackathonwriteservice.team.repository;

import com.hackathonorganizer.hackathonwriteservice.team.model.InvitationStatus;
import com.hackathonorganizer.hackathonwriteservice.team.model.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    boolean existsByToUserIdAndTeamIdAndInvitationStatus(Long toUserKeycloakId, Long teamId, InvitationStatus invitationStatus);
}
