package com.codearena.controller;

import com.codearena.dto.request.ContestRequest;
import com.codearena.dto.response.ContestResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.service.ContestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @GetMapping
    public ResponseEntity<PageResponse<ContestResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ContestResponse> result = contestService.getAll(search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ContestResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(contestService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<ContestResponse> create(@Valid @RequestBody ContestRequest request) {
        return ResponseEntity.ok(contestService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContestResponse> update(@PathVariable Long id, @Valid @RequestBody ContestRequest request) {
        return ResponseEntity.ok(contestService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}