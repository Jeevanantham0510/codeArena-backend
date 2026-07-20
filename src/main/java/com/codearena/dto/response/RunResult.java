package com.codearena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RunResult {
    private boolean success;
    private String stdout;
    private String stderr;
    private long executionTimeMs;
}
