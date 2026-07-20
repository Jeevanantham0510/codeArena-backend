package com.codearena.service;

import com.codearena.dto.request.ContestRequest;
import com.codearena.dto.response.ContestResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.entity.Contest;
import com.codearena.entity.ContestProblem;
import com.codearena.entity.Problem;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.repository.ContestRepository;
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
public class ContestService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;

    @Transactional
    public PageResponse<ContestResponse> getAll(String search, Pageable pageable) {
        Page<Contest> result = (search == null || search.isBlank())
                ? contestRepository.findAll(pageable)
                : contestRepository.findByTitleContainingIgnoreCase(search.trim(), pageable);
        return PageResponse.of(result.map(this::toResponse));
    }

    @Transactional
    public ContestResponse getBySlug(String slug) {
        Contest contest = contestRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + slug));
        return toResponse(contest);
    }

    @Transactional
    public Contest getEntityById(Long id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + id));
    }

    @Transactional
    public ContestResponse create(ContestRequest request) {
        String slug = toSlug(request.getTitle());
        int suffix = 1;
        String candidate = slug;
        while (contestRepository.existsBySlug(candidate)) {
            candidate = slug + "-" + suffix++;
        }

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .slug(candidate)
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        attachProblems(contest, request.getProblemIds());
        contestRepository.save(contest);
        return toResponse(contest);
    }

    @Transactional
    public ContestResponse update(Long id, ContestRequest request) {
        Contest contest = getEntityById(id);
        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());

        contest.getContestProblems().clear();
        attachProblems(contest, request.getProblemIds());

        contestRepository.save(contest);
        return toResponse(contest);
    }

    public void delete(Long id) {
        if (!contestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contest not found with id: " + id);
        }
        contestRepository.deleteById(id);
    }

    private void attachProblems(Contest contest, List<Long> problemIds) {
        if (problemIds == null) return;
        int order = 0;
        for (Long problemId : problemIds) {
            Problem problem = problemRepository.findById(problemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));
            contest.getContestProblems().add(ContestProblem.builder()
                    .contest(contest)
                    .problem(problem)
                    .orderIndex(order++)
                    .build());
        }
    }

    // Resolves the lazy contestProblems collection while the transaction is
    // still open, so callers never hand a lazy proxy to Jackson.
    private ContestResponse toResponse(Contest contest) {
        List<ContestResponse.ProblemSummary> problems = contest.getContestProblems().stream()
                .sorted((a, b) -> a.getOrderIndex().compareTo(b.getOrderIndex()))
                .map(cp -> ContestResponse.ProblemSummary.builder()
                        .id(cp.getProblem().getId())
                        .title(cp.getProblem().getTitle())
                        .slug(cp.getProblem().getSlug())
                        .difficulty(cp.getProblem().getDifficulty())
                        .build())
                .collect(Collectors.toList());

        return ContestResponse.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .slug(contest.getSlug())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .createdAt(contest.getCreatedAt())
                .problems(problems)
                .build();
    }

    private String toSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}