package com.codearena.repository;

import com.codearena.entity.Language;
import com.codearena.entity.Submission;
import com.codearena.entity.SubmissionStatus;
import com.codearena.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT s FROM Submission s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:language IS NULL OR s.language = :language)")
    Page<Submission> filter(@Param("status") SubmissionStatus status,
                             @Param("language") Language language,
                             Pageable pageable);

    long countByStatus(SubmissionStatus status);

    long countByCreatedAtAfter(LocalDateTime after);
}
