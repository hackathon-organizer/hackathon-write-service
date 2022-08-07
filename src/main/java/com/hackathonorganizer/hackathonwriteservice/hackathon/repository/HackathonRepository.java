package com.hackathonorganizer.hackathonwriteservice.hackathon.repository;

import com.hackathonorganizer.hackathonwriteservice.hackathon.model.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
}
