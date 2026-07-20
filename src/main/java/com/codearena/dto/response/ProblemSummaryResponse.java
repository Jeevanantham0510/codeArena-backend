package com.codearena.dto.response;

import com.codearena.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProblemSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private Difficulty difficulty;
}
