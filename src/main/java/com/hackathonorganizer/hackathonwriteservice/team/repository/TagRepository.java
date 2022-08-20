package com.hackathonorganizer.hackathonwriteservice.team.repository;

import com.hackathonorganizer.hackathonwriteservice.team.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Tag> findByNameIgnoreCase(String name);
}
