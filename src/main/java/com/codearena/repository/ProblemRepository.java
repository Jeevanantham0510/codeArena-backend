package com.codearena.repository;

import com.codearena.entity.Difficulty;
import com.codearena.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Page<Problem> findByDifficulty(Difficulty difficulty, Pageable pageable);
    Page<Problem> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Problem> findByDifficultyAndTitleContainingIgnoreCase(Difficulty difficulty, String title, Pageable pageable);
}
