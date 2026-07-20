package com.codearena.dto.request;

import com.codearena.entity.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProblemRequest {

    @NotBlank
    private String title;

    @NotNull
    private Difficulty difficulty;

    @NotBlank
    private String description;

    private String constraints;

    private String inputFormat;

    private String outputFormat;

    private Integer timeLimitMs = 2000;

    private Integer memoryLimitMb = 256;

    @NotEmpty
    @Valid
    private List<TestCaseRequest> testCases;
}
