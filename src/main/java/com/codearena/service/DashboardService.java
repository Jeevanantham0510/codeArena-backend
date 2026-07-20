package com.codearena.service;

import com.codearena.dto.response.DashboardStatsResponse;
import com.codearena.entity.Difficulty;
import com.codearena.entity.Submission;
import com.codearena.entity.SubmissionStatus;
import com.codearena.repository.ContestRepository;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final ContestRepository contestRepository;

    public DashboardStatsResponse getStats() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (SubmissionStatus status : SubmissionStatus.values()) {
            byStatus.put(status.name(), submissionRepository.countByStatus(status));
        }

        Map<String, Long> byDifficulty = new LinkedHashMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            byDifficulty.put(difficulty.name(),
                    problemRepository.findByDifficulty(difficulty, PageRequest.of(0, 1)).getTotalElements());
        }

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalProblems(problemRepository.count())
                .totalSubmissions(submissionRepository.count())
                .totalContests(contestRepository.count())
                .submissionsByStatus(byStatus)
                .problemsByDifficulty(byDifficulty)
                .submissionsLast7Days(buildLast7DaySeries())
                .recentActivity(buildRecentActivity())
                .build();
    }

    private List<DashboardStatsResponse.DailyCount> buildLast7DaySeries() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(6).atStartOfDay();

        Map<String, Long> counts = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            counts.put(today.minusDays(i).format(fmt), 0L);
        }

        // For a small/medium dataset this in-memory pass is simplest; swap for a
        // native GROUP BY query if submission volume grows large.
        submissionRepository.findAll().stream()
                .filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(since))
                .forEach(s -> {
                    String key = s.getCreatedAt().toLocalDate().format(fmt);
                    counts.computeIfPresent(key, (k, v) -> v + 1);
                });

        List<DashboardStatsResponse.DailyCount> series = new ArrayList<>();
        counts.forEach((date, count) -> series.add(new DashboardStatsResponse.DailyCount(date, count)));
        return series;
    }

    private List<DashboardStatsResponse.RecentActivity> buildRecentActivity() {
        List<DashboardStatsResponse.RecentActivity> activity = new ArrayList<>();

        submissionRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .forEach(s -> activity.add(new DashboardStatsResponse.RecentActivity(
                        "SUBMISSION",
                        describeSubmission(s),
                        String.valueOf(s.getCreatedAt())
                )));

        userRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .forEach(u -> activity.add(new DashboardStatsResponse.RecentActivity(
                        "NEW_USER",
                        u.getUsername() + " joined CodeArena",
                        String.valueOf(u.getCreatedAt())
                )));

        activity.sort((a, b) -> safeParse(b.getTimestamp()).compareTo(safeParse(a.getTimestamp())));
        return activity.size() > 10 ? activity.subList(0, 10) : activity;
    }

    private String describeSubmission(Submission s) {
        String user = s.getUser() != null ? s.getUser().getUsername() : "someone";
        String problem = s.getProblem() != null ? s.getProblem().getTitle() : "a problem";
        return user + " submitted " + problem + " \u2192 " + s.getStatus();
    }

    private LocalDateTime safeParse(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            return LocalDateTime.MIN;
        }
    }
}
