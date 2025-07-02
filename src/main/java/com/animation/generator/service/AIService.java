package com.animation.generator.service;

import com.animation.generator.dtos.UserRequest;
import com.animation.generator.objects.Diagram;
import com.animation.generator.repository.DiagramRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AIService {

    @Autowired
    private DiagramRepository diagramRepository;
    @Value("${spring.llm.api.key:AIzaSyAHixz2d4aHmP38AOoJmjXmA6f2mjOl7nw}")
    private String apiKey;
    private String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

    public Diagram generateDiagramFromPrompt(UserRequest userRequest) {
        if (userRequest == null || userRequest.getPrompt() == null) {
            return null;
        }

        try {
            String fullPrompt = buildPromptWithHistory(userRequest);
            String llmResponseJson = queryLLM(fullPrompt);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(llmResponseJson);

            String manimCode = root.get("manimCode").asText();
            JsonNode jsonData = root.get("jsonData");
            Diagram diagram = new Diagram();
            diagram.setUserId(userRequest.getUserId());
            diagram.setPrompt(userRequest.getPrompt());
            diagram.setGeneratedCode(manimCode);
            diagram.setJsonRepresentation(jsonData.toPrettyString());
            diagram.setChatId(userRequest.getChatId());

            Path videoFile = renderWithDocker(manimCode, jsonData);
            String videoUrl = uploadVideo(videoFile);

            diagram.setVideoSource(videoUrl);
            diagramRepository.save(diagram);
            Files.deleteIfExists(videoFile);
            return diagram;
        } catch (Exception e) {
            log.error("Error generating diagram with Docker", e);
            return null;
        }
    }

    private String buildPromptWithHistory(UserRequest userRequest) {
        List<Diagram> previousDiagrams = diagramRepository.findByChatId(userRequest.getChatId());
        StringBuilder contextBuilder = new StringBuilder();

        int start = Math.max(0, previousDiagrams.size() - 2);
        for (int i = start; i < previousDiagrams.size(); i++) {
            Diagram d = previousDiagrams.get(i);
            contextBuilder.append("Previous Prompt: ").append(d.getPrompt()).append("\n");
            contextBuilder.append("Previous JSON: ").append(d.getJsonRepresentation()).append("\n\n");
        }

        contextBuilder.append("Current Prompt: ").append(userRequest.getPrompt()).append("\n");

        return """
                You are an AI tool that generates 2D animated architecture diagrams based on natural language.
                
                Given a prompt from the user, return a JSON object with the following structure:
                
                {
                  "jsonData": { ... },         // Structured architecture representation (used for storage)
                  "manimCode": "<python-code>" // Complete Manim script that renders the animation to an MP4 file
                }
                
                Requirements:
                - The Python code must define a class called `ArchitectureDiagram(Scene)`
                - The code must generate a full 2D animation using Manim (Text, Rectangle, Arrow)
                - The animation must be rendered and saved as an MP4 (not a preview)
                - Use standard Manim constructs (no Tex, LaTeX, or SVGs)
                - Do not include any explanation or markdown — only valid JSON with the structure above
                
                Design Guidelines:
                - Use consistent padding and spacing between elements
                - Align components to a clean visual grid
                - Ensure text stays inside or clearly beside shapes — no overlap
                - Use rounded rectangles for UI/frontend, plain rectangles for services, ellipses for databases
                - Avoid arrows crossing or overlapping
                - Use uniform font sizes and styles for similar components
                - If there are too many components, organize them into horizontal or vertical layers
                
                Visual Themes:
                - Let the user or the model choose a layout style (e.g., "classic", "minimalist", "layered", etc.)
                - Apply a consistent theme across the entire diagram for visual clarity
                
                Contextual Continuity:
                - If the prompt is similar to a previous one, reuse and evolve the previous JSON representation
                - Use only the most recent two exchanges for continuity if relevant
                
                Prompt: %s
                """.formatted(contextBuilder.toString());
    }

    private String queryLLM(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = Map.of("contents", List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL, request, Map.class);
        return extractTextFromGeminiResponse(response);
    }

    private String extractTextFromGeminiResponse(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return parts.get(0).get("text").toString().replaceAll("(?s)```.*?\\n", "").replaceAll("```", "").trim();
    }

    private Path renderWithDocker(String manimCode, JsonNode jsonData) throws IOException, InterruptedException {
        String id = UUID.randomUUID().toString();
        Path basePath = Paths.get("render-engine").toAbsolutePath();
        Path diagramsDir = basePath.resolve("diagrams");
        Path outputDir = basePath.resolve("output/videos");
        Files.createDirectories(diagramsDir);
        Files.createDirectories(outputDir);
        Path pyFile = diagramsDir.resolve(id + ".py");
        Path jsonFile = diagramsDir.resolve(id + ".json");
        Path videoFile = outputDir.resolve(id + ".mp4");
        Files.writeString(pyFile, manimCode);
        Files.writeString(jsonFile, jsonData.toPrettyString());
        String containerOutputPath = "/app/output/" + id + ".mp4";
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", diagramsDir + ":/app/diagrams",
                "-v", outputDir + ":/app/output",
                "manim-api",
                "diagrams/" + id + ".py",
                "ArchitectureDiagram",
                "--output_file", containerOutputPath
        );

        pb.inheritIO();
        Process dockerProcess = pb.start();
        if (dockerProcess.waitFor() != 0) {
            throw new RuntimeException("Docker rendering failed.");
        }
        Files.deleteIfExists(pyFile);
        Files.deleteIfExists(jsonFile);
        return videoFile;
    }

    private String uploadVideo(Path videoFilePath) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        FileSystemResource videoResource = new FileSystemResource(videoFilePath.toFile());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", videoResource);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://upload.gofile.io/uploadfile", requestEntity, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Upload to GoFile failed: " + response.getBody());
        }
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        return root.path("data").path("downloadPage").asText();
    }

}
