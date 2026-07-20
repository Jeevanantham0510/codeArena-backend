package com.codearena.dto.response;

import com.codearena.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class AdminProblemSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private Difficulty difficulty;
    private int testCaseCount;
    private LocalDateTime createdAt;
}
