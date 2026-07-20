package com.codearena.dto.response;

import com.codearena.entity.Difficulty;
import com.codearena.entity.Language;
import com.codearena.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Flat, serialization-safe view of a Submission. Built explicitly inside a
 * transactional service method so lazy `user`/`problem` associations are
 * resolved before the method returns — never left as lazy proxies for
 * Jackson to touch later (which is what caused the 500s).
 */
@Data
@Builder
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private UserSummary user;
    private ProblemSummary problem;
    private Language language;
    private SubmissionStatus status;
    private String code;
    private Long executionTimeMs;
    private Integer totalTestCases;
    private Integer passedTestCases;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String username;
    }

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