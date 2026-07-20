package com.codearena.dto.response;

import com.codearena.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProblemDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private Difficulty difficulty;
    private String description;
    private String constraints;
    private String inputFormat;
    private String outputFormat;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private List<TestCaseResponse> visibleTestCases;
}
