package com.codearena.service;

import com.codearena.dto.request.RunRequest;
import com.codearena.dto.request.SubmitRequest;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.RunResult;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.dto.response.SubmissionResult;
import com.codearena.entity.*;
import com.codearena.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final JudgeService judgeService;
    private final ProblemService problemService;
    private final SubmissionRepository submissionRepository;

    public RunResult run(RunRequest request) {
        JudgeService.ExecResult result = judgeService.execute(
                request.getLanguage(), request.getCode(), request.getCustomInput(), 5000);

        return RunResult.builder()
                .success(result.success())
                .stdout(result.stdout())
                .stderr(result.stderr())
                .executionTimeMs(result.timeMs())
                .build();
    }

    public SubmissionResult submit(User user, Long problemId, SubmitRequest request) {
        Problem problem = problemService.getEntityById(problemId);
        List<TestCase> testCases = problem.getTestCases();

        int passed = 0;
        long maxTime = 0;
        SubmissionStatus status = SubmissionStatus.ACCEPTED;
        String failureMessage = null;

        for (TestCase tc : testCases) {
            JudgeService.ExecResult result = judgeService.execute(
                    request.getLanguage(), request.getCode(), tc.getInput(), problem.getTimeLimitMs());

            maxTime = Math.max(maxTime, result.timeMs());

            if (result.timedOut()) {
                status = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                failureMessage = "Exceeded time limit of " + problem.getTimeLimitMs() + "ms";
                break;
            }
            if (!result.success()) {
                boolean looksLikeCompileError = result.stderr() != null &&
                        (result.stderr().toLowerCase().contains("error") && testCases.indexOf(tc) == 0);
                status = looksLikeCompileError ? SubmissionStatus.COMPILATION_ERROR : SubmissionStatus.RUNTIME_ERROR;
                failureMessage = result.stderr();
                break;
            }
            if (!result.stdout().trim().equals(tc.getExpectedOutput().trim())) {
                status = SubmissionStatus.WRONG_ANSWER;
                failureMessage = "Wrong answer on test case " + (testCases.indexOf(tc) + 1);
                break;
            }
            passed++;
        }

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .language(request.getLanguage())
                .code(request.getCode())
                .status(status)
                .executionTimeMs(maxTime)
                .totalTestCases(testCases.size())
                .passedTestCases(passed)
                .build();

        submissionRepository.save(submission);

        return SubmissionResult.builder()
                .submissionId(submission.getId())
                .status(status)
                .totalTestCases(testCases.size())
                .passedTestCases(passed)
                .executionTimeMs(maxTime)
                .failureMessage(failureMessage)
                .build();
    }

    @Transactional
    public List<SubmissionResponse> getHistory(User user) {
        return submissionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- Admin-only: browse all submissions with verdict/language filters ----
    @Transactional
    public PageResponse<SubmissionResponse> getAllForAdmin(SubmissionStatus status, Language language, Pageable pageable) {
        Page<Submission> result = submissionRepository.filter(status, language, pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    // Resolves lazy user/problem associations while the transaction is still
    // open, so callers never hand a lazy proxy to Jackson.
    private SubmissionResponse toResponse(Submission s) {
        SubmissionResponse.UserSummary userSummary = null;
        if (s.getUser() != null) {
            userSummary = SubmissionResponse.UserSummary.builder()
                    .id(s.getUser().getId())
                    .username(s.getUser().getUsername())
                    .build();
        }

        SubmissionResponse.ProblemSummary problemSummary = null;
        if (s.getProblem() != null) {
            problemSummary = SubmissionResponse.ProblemSummary.builder()
                    .id(s.getProblem().getId())
                    .title(s.getProblem().getTitle())
                    .slug(s.getProblem().getSlug())
                    .difficulty(s.getProblem().getDifficulty())
                    .build();
        }

        return SubmissionResponse.builder()
                .id(s.getId())
                .user(userSummary)
                .problem(problemSummary)
                .language(s.getLanguage())
                .status(s.getStatus())
                .code(s.getCode())
                .executionTimeMs(s.getExecutionTimeMs())
                .totalTestCases(s.getTotalTestCases())
                .passedTestCases(s.getPassedTestCases())
                .createdAt(s.getCreatedAt())
                .build();
    }
}