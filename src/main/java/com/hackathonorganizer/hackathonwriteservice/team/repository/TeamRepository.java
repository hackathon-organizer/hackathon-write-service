package com.hackathonorganizer.hackathonwriteservice.team.repository;

import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import com.hackathonorganizer.hackathonwriteservice.team.utils.model.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface TeamRepository extends JpaRepository<Team, Long> {

//    Set<TeamInvitation> findInvitationsById(String id);
}
