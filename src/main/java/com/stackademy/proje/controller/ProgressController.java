package com.stackademy.proje.controller;

import com.stackademy.proje.dto.ProgressUpdateRequest;
import com.stackademy.proje.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    // POST /api/progress/update
    @PostMapping("/update")
    public ResponseEntity<String> updateProgress(@RequestBody ProgressUpdateRequest request) {
        return ResponseEntity.ok(progressService.updateProgress(request));
    }
}