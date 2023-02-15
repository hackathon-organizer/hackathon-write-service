package com.hackathonorganizer.hackathonwriteservice.hackathon.repository;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    boolean existsCriteriaByNameAndHackathonId(String name, Long hackathonId);
}