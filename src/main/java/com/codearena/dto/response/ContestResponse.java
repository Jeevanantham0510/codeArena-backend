package com.codearena.dto.response;

import com.codearena.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flat, serialization-safe view of a Contest. Built inside a transactional
 * service method so the lazy `contestProblems` collection is resolved
 * before the method returns — same fix as SubmissionResponse.
 */
@Data
@Builder
@AllArgsConstructor
public class ContestResponse {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private List<ProblemSummary> problems;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ProblemSummary {
        private Long id;
        private String title;
        private String slug;
        private Difficulty difficulty;
    }
}
