package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.AiTutorRequest;
import org.skillbridge.dto.response.AiTutorResponse;
import org.skillbridge.service.impl.AiTutorServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Tutor")
public class AiTutorController {

    private final AiTutorServiceImpl service;

    public AiTutorController(AiTutorServiceImpl service) { this.service = service; }

    @PostMapping("/tutor")
    public ResponseEntity<AiTutorResponse> ask(@Valid @RequestBody AiTutorRequest req) {
        return ResponseEntity.ok(service.ask(req));
    }
}