package com.codearena.controller;

import com.codearena.dto.request.RunRequest;
import com.codearena.dto.request.SubmitRequest;
import com.codearena.dto.response.RunResult;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.dto.response.SubmissionResult;
import com.codearena.entity.User;
import com.codearena.repository.UserRepository;
import com.codearena.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserRepository userRepository;

    @PostMapping("/run")
    public ResponseEntity<RunResult> run(@Valid @RequestBody RunRequest request) {
        return ResponseEntity.ok(submissionService.run(request));
    }

    @PostMapping("/submit/{problemId}")
    public ResponseEntity<SubmissionResult> submit(@PathVariable Long problemId,
                                                   @Valid @RequestBody SubmitRequest request,
                                                   Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(submissionService.submit(user, problemId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubmissionResponse>> myHistory(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(submissionService.getHistory(user));
    }

    private User currentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}