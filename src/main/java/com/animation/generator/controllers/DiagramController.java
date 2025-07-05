package com.animation.generator.controllers;

import com.animation.generator.dtos.UserRequest;
import com.animation.generator.objects.Diagram;
import com.animation.generator.service.AIService;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/diagrams")

public class DiagramController {
    @Autowired
    private AIService aiService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateDiagram(@RequestBody UserRequest userRequest) {
        JsonNode response = aiService.generateDiagramFromPrompt(userRequest);
        return ResponseEntity.ok(response);
    }

}
