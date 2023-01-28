package com.hackathonorganizer.hackathonwriteservice.hackathon.repository;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.CriteriaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CriteriaAnswerRepository extends JpaRepository<CriteriaAnswer, Long> {

    boolean existsByCriteriaIdAndUserId(Long criteriaId, Long userId);
}
