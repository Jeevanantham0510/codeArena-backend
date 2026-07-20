package com.codearena.service;

import com.codearena.dto.request.ProblemRequest;
import com.codearena.dto.request.TestCaseRequest;
import com.codearena.dto.response.AdminProblemDetailResponse;
import com.codearena.dto.response.AdminProblemSummaryResponse;
import com.codearena.dto.response.AdminTestCaseResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.ProblemDetailResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.dto.response.TestCaseResponse;
import com.codearena.entity.Difficulty;
import com.codearena.entity.Problem;
import com.codearena.entity.TestCase;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    public List<ProblemSummaryResponse> getAll() {
        return problemRepository.findAll().stream()
                .map(p -> new ProblemSummaryResponse(p.getId(), p.getTitle(), p.getSlug(), p.getDifficulty()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProblemDetailResponse getBySlug(String slug) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + slug));
        return toDetailResponse(problem);
    }

    @Transactional
    public Problem getEntityById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
    }

    @Transactional
    public ProblemDetailResponse create(ProblemRequest request) {
        String slug = toSlug(request.getTitle());
        int suffix = 1;
        String candidate = slug;
        while (problemRepository.existsBySlug(candidate)) {
            candidate = slug + "-" + suffix++;
        }

        Problem problem = Problem.builder()
                .title(request.getTitle())
                .slug(candidate)
                .difficulty(request.getDifficulty())
                .description(request.getDescription())
                .constraints(request.getConstraints())
                .inputFormat(request.getInputFormat())
                .outputFormat(request.getOutputFormat())
                .timeLimitMs(request.getTimeLimitMs())
                .memoryLimitMb(request.getMemoryLimitMb())
                .build();

        for (TestCaseRequest tc : request.getTestCases()) {
            problem.getTestCases().add(TestCase.builder()
                    .problem(problem)
                    .input(tc.getInput())
                    .expectedOutput(tc.getExpectedOutput())
                    .hidden(tc.isHidden())
                    .build());
        }

        problemRepository.save(problem);
        return toDetailResponse(problem);
    }

    @Transactional
    public ProblemDetailResponse update(Long id, ProblemRequest request) {
        Problem problem = getEntityById(id);
        problem.setTitle(request.getTitle());
        problem.setDifficulty(request.getDifficulty());
        problem.setDescription(request.getDescription());
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitMb(request.getMemoryLimitMb());

        problem.getTestCases().clear();
        for (TestCaseRequest tc : request.getTestCases()) {
            problem.getTestCases().add(TestCase.builder()
                    .problem(problem)
                    .input(tc.getInput())
                    .expectedOutput(tc.getExpectedOutput())
                    .hidden(tc.isHidden())
                    .build());
        }

        problemRepository.save(problem);
        return toDetailResponse(problem);
    }

    public void delete(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    // ---- Admin-only reads: include hidden test cases and support filtering/pagination ----

    @Transactional
    public PageResponse<AdminProblemSummaryResponse> getAllForAdmin(Difficulty difficulty, String search, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        Page<Problem> result;

        if (difficulty != null && hasSearch) {
            result = problemRepository.findByDifficultyAndTitleContainingIgnoreCase(difficulty, search.trim(), pageable);
        } else if (difficulty != null) {
            result = problemRepository.findByDifficulty(difficulty, pageable);
        } else if (hasSearch) {
            result = problemRepository.findByTitleContainingIgnoreCase(search.trim(), pageable);
        } else {
            result = problemRepository.findAll(pageable);
        }

        return PageResponse.of(result.map(p -> AdminProblemSummaryResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .difficulty(p.getDifficulty())
                .testCaseCount(p.getTestCases().size())
                .createdAt(p.getCreatedAt())
                .build()));
    }

    @Transactional
    public AdminProblemDetailResponse getByIdForAdmin(Long id) {
        Problem problem = getEntityById(id);

        List<AdminTestCaseResponse> testCases = problem.getTestCases().stream()
                .map(tc -> AdminTestCaseResponse.builder()
                        .id(tc.getId())
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .hidden(tc.isHidden())
                        .build())
                .collect(Collectors.toList());

        return AdminProblemDetailResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .slug(problem.getSlug())
                .difficulty(problem.getDifficulty())
                .description(problem.getDescription())
                .constraints(problem.getConstraints())
                .inputFormat(problem.getInputFormat())
                .outputFormat(problem.getOutputFormat())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .testCases(testCases)
                .build();
    }

    private ProblemDetailResponse toDetailResponse(Problem problem) {
        List<TestCaseResponse> visible = problem.getTestCases().stream()
                .filter(tc -> !tc.isHidden())
                .map(tc -> new TestCaseResponse(tc.getId(), tc.getInput(), tc.getExpectedOutput()))
                .collect(Collectors.toList());

        return ProblemDetailResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .slug(problem.getSlug())
                .difficulty(problem.getDifficulty())
                .description(problem.getDescription())
                .constraints(problem.getConstraints())
                .inputFormat(problem.getInputFormat())
                .outputFormat(problem.getOutputFormat())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .visibleTestCases(visible)
                .build();
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
