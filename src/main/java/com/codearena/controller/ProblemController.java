package com.codearena.controller;

import com.codearena.dto.request.ProblemRequest;
import com.codearena.dto.response.ProblemDetailResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemSummaryResponse>> getAll() {
        return ResponseEntity.ok(problemService.getAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProblemDetailResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(problemService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<ProblemDetailResponse> create(@Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(problemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemDetailResponse> update(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(problemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        problemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
