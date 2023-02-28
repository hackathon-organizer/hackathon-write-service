package com.hackathonorganizer.hackathonwriteservice.team.repository;

import com.hackathonorganizer.hackathonwriteservice.team.model.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Override
    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "teamMembersIds",
                    "hackathon",
                    "tags",
                    "invitations"
            }
    )
    Optional<Team> findById(Long teamId);
}
