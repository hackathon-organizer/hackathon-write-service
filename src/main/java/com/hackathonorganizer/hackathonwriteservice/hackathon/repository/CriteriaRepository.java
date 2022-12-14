package com.hackathonorganizer.hackathonwriteservice.hackathon.repository;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    List<Criteria> findCriteriaByHackathonId(Long hackathonId);

    boolean existsCriteriaByNameAndHackathonId(String name, Long hackathonId);
}