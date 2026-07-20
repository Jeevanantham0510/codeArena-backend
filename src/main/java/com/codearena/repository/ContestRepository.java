package com.codearena.repository;

import com.codearena.entity.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    Optional<Contest> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Contest> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
