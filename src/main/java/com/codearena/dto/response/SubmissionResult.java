package com.codearena.dto.response;

import com.codearena.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SubmissionResult {
    private Long submissionId;
    private SubmissionStatus status;
    private int totalTestCases;
    private int passedTestCases;
    private long executionTimeMs;
    private String failureMessage;
}
