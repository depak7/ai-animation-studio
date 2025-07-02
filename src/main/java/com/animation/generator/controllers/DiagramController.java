package com.animation.generator.controllers;

import com.animation.generator.dtos.UserRequest;
import com.animation.generator.objects.Diagram;
import com.animation.generator.service.AIService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagrams")
@RequiredArgsConstructor
public class DiagramController {
    @Autowired
    private AIService aiService;

    @PostMapping("/generate")
    public ResponseEntity<Diagram> generateDiagram(@RequestBody UserRequest userRequest) {
        Diagram diagram = aiService.generateDiagramFromPrompt(userRequest);
        if (diagram == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        return ResponseEntity.ok(diagram);
    }
}
