package com.codearena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AdminTestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private boolean hidden;
}
