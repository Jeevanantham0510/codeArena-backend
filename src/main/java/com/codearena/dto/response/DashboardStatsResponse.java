package com.codearena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalProblems;
    private long totalSubmissions;
    private long totalContests;

    private Map<String, Long> submissionsByStatus;
    private Map<String, Long> problemsByDifficulty;
    private List<DailyCount> submissionsLast7Days;
    private List<RecentActivity> recentActivity;

    @Data
    @Builder
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private long count;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RecentActivity {
        private String type;
        private String message;
        private String timestamp;
    }
}
