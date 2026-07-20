package com.codearena.controller;

import com.codearena.dto.request.RoleUpdateRequest;
import com.codearena.dto.response.AdminProblemDetailResponse;
import com.codearena.dto.response.AdminProblemSummaryResponse;
import com.codearena.dto.response.DashboardStatsResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.entity.Difficulty;
import com.codearena.entity.Language;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.entity.SubmissionStatus;
import com.codearena.entity.User;
import com.codearena.service.DashboardService;
import com.codearena.service.ProblemService;
import com.codearena.service.SubmissionService;
import com.codearena.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All routes here require ROLE_ADMIN — enforced in SecurityConfig via
 * `.requestMatchers("/api/admin/**").hasRole("ADMIN")`.
 *
 * Problem/contest *mutations* stay on their existing public controllers
 * (/api/problems, /api/contests), which are already admin-gated for
 * POST/PUT/DELETE. This controller adds the admin-only reads (dashboard,
 * user management, submission browsing, and problem views that include
 * hidden test cases) that don't belong on public endpoints.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final ProblemService problemService;
    private final SubmissionService submissionService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> dashboard() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    // ---- Users ----

    @GetMapping("/users")
    public ResponseEntity<PageResponse<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userService.getAll(search, pageable));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<User> updateUserRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request.getRole()));
    }

    // ---- Problems (admin reads only; writes go through /api/problems) ----

    @GetMapping("/problems")
    public ResponseEntity<PageResponse<AdminProblemSummaryResponse>> getProblems(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(problemService.getAllForAdmin(difficulty, search, pageable));
    }

    @GetMapping("/problems/{id}")
    public ResponseEntity<AdminProblemDetailResponse> getProblem(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getByIdForAdmin(id));
    }

    // ---- Submissions ----

    @GetMapping("/submissions")
    public ResponseEntity<PageResponse<SubmissionResponse>> getSubmissions(
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(required = false) Language language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(submissionService.getAllForAdmin(status, language, pageable));
    }
}