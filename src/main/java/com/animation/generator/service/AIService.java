package com.animation.generator.service;

import com.animation.generator.controllers.RenderSseController;
import com.animation.generator.dtos.UserRequest;
import com.animation.generator.objects.Diagram;
import com.animation.generator.repository.DiagramRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AIService {

    @Autowired
    private DiagramRepository   diagramRepository;
    @Autowired
    private RenderSseController renderSseController;
    @Value("${spring.llm.api.key}")
    private String              apiKey;
    @Autowired
    private ObjectMapper        objectMapper;
    @Autowired
    private ChatService         chatService;
    @Autowired
    private HttpServletRequest request;
    private final String        BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public JsonNode generateDiagramFromPrompt(UserRequest userRequest) {
        ObjectNode response = objectMapper.createObjectNode();
        try {
            Long userId =request.getAttribute("userId") != null ? (Long) request.getAttribute("userId") : 0;
            String guestId = (String) request.getAttribute("guestId");
            if (userRequest == null || userRequest.getPrompt() == null || userRequest.getPrompt().isEmpty() || userRequest.getConversationId() == null
                    || userRequest.getConversationId().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing or invalid request parameters.");
                return response;
            }
            long existingChatId = userRequest.getChatId();
            if (existingChatId <= 0) {
                String chatTitle = userRequest.getPrompt().length() > 100 ? userRequest.getPrompt().substring(0, 100) : userRequest.getPrompt();
                JsonNode chatJson = objectMapper.createObjectNode().put("title", chatTitle).put("userId", userId).put("guestId", guestId);

                Long chatId = chatService.createChat(chatJson);
                if (chatId > 0) {
                    log.info("Created new chat with id {}", chatId);
                    userRequest.setChatId(chatId);
                } else {
                    response.put("success", false);
                    response.put("message", "Failed to create chat.");
                    return response;
                }

            } else {
                boolean exists = chatService.checkIfChatExists(userRequest.getChatId());
                if (!exists) {
                    response.put("success", false);
                    response.put("message", "Chat ID does not exist.");
                    return response;
                }
            }
            String fullPrompt = buildPromptWithHistory(userRequest);
            String llmResponseJson = queryLLM(fullPrompt);
            JsonNode root = objectMapper.readTree(llmResponseJson);

            if (!root.has("manimCode") || !root.has("jsonData")) {
                response.put("success", false);
                response.put("message", "Invalid LLM response.");
                return response;
            }

            String manimCode = root.get("manimCode").asText();
            JsonNode jsonData = root.get("jsonData");

            Diagram diagram = new Diagram();
            diagram.setUserId(userId);
            diagram.setGuestId(guestId);
            diagram.setPrompt(userRequest.getPrompt());
            diagram.setGeneratedCode(manimCode);
            diagram.setJsonRepresentation(jsonData.toPrettyString());
            diagram.setChatId(userRequest.getChatId());

            Path videoFile = renderWithDocker(userRequest.getConversationId(), manimCode, jsonData);
            if (videoFile == null) {
                response.put("success", false);
                response.put("message", "Video rendering failed.");
                return response;
            }

            String videoUrl = uploadVideo(videoFile);
            diagram.setVideoSource(videoUrl);

            diagramRepository.save(diagram);
            Files.deleteIfExists(videoFile);

            response.put("success", true);
            response.set("diagram", objectMapper.valueToTree(diagram));
            return response;

        } catch (Exception e) {
            log.error("Error generating diagram with Docker", e);
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return response;
        }
    }

    private String buildPromptWithHistory(UserRequest userRequest) throws JsonProcessingException {
        List<Diagram> previousDiagrams = diagramRepository.findByChatId(userRequest.getChatId());
        StringBuilder contextBuilder = new StringBuilder();

        int start = Math.max(0, previousDiagrams.size() - 2);
        for (int i = start; i < previousDiagrams.size(); i++) {
            Diagram d = previousDiagrams.get(i);
            contextBuilder.append("Previous Prompt: ").append(d.getPrompt()).append("\n");
            JsonNode jsonNode = objectMapper.readTree(d.getJsonRepresentation());
            contextBuilder.append("Previous JSON: ").append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)).append("\n\n");
            contextBuilder.append("Previous JSON: ").append(d.getJsonRepresentation()).append("\n\n");
        }

        contextBuilder.append("Current Prompt: ").append(userRequest.getPrompt()).append("\n");

        return """
                You are an AI tool that generates 2D animated architecture diagrams based on natural language.

                Given a prompt from the user, return a JSON object with the following structure:k

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
        String url = BASE_URL + "?key=" + apiKey.trim();
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = Map.of("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        return extractTextFromGeminiResponse(response);
    }

    private String extractTextFromGeminiResponse(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return parts.get(0).get("text").toString().replaceAll("(?s)```.*?\\n", "").replaceAll("```", "").trim();
    }

    private Path renderWithDocker(String conversationId, String manimCode, JsonNode jsonData) throws IOException, InterruptedException {
        Path basePath = Paths.get("render-engine").toAbsolutePath();
        Path diagramsDir = basePath.resolve("diagrams");
        Path outputDir = basePath.resolve("output/videos");
        Files.createDirectories(diagramsDir);
        Files.createDirectories(outputDir);
        Path pyFile = diagramsDir.resolve(conversationId + ".py");
        Path jsonFile = diagramsDir.resolve(conversationId + ".json");
        Path videoFile = outputDir.resolve(conversationId + ".mp4");
        Files.writeString(pyFile, manimCode);
        Files.writeString(jsonFile, jsonData.toPrettyString());
        String containerOutputPath = "/app/output/" + conversationId + ".mp4";
        ProcessBuilder pb = new ProcessBuilder("docker", "run", "--rm", "-v", diagramsDir + ":/app/diagrams", "-v", outputDir + ":/app/output",
                "manim-api", "diagrams/" + conversationId + ".py", "ArchitectureDiagram", "--output_file", containerOutputPath);
        Process dockerProcess = pb.start();
        pb.redirectErrorStream(true);
        BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(dockerProcess.getInputStream()));

        String infoLine;
        while ((infoLine = stdOutReader.readLine()) != null) {
            String clean = extractRelevantLine(infoLine);
            if (clean != null && !clean.isEmpty()) {
                renderSseController.sendLog(conversationId, clean);
            }
        }
        renderSseController.sendLog(conversationId, "All done! You can now download or preview the animation.");
        renderSseController.complete(conversationId);

        if (dockerProcess.waitFor() != 0) {
            throw new RuntimeException("Docker rendering failed.");
        }
        Files.deleteIfExists(pyFile);
        Files.deleteIfExists(jsonFile);
        return videoFile;
    }

    private String uploadVideo(Path videoFilePath) throws IOException {
        String SUPABASE_URL = "https://jnfduhojxxnfyjwzmfnk.supabase.co";
        String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpuZmR1aG9qeHhuZnlqd3ptZm5rIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MTUyNTU1NywiZXhwIjoyMDY3MTAxNTU3fQ.sZifqnhHirvOgWYhM9vqfDsAmsN4Pk2yg3oS0wvRs_s";
        String BUCKET_NAME = "ai-animator";

        RestTemplate restTemplate = new RestTemplate();
        String fileName = videoFilePath.getFileName().toString();

        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("x-upsert", "true");

        byte[] fileBytes = Files.readAllBytes(videoFilePath);
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload to Supabase failed: " + response.getBody());
        }
        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
    }

    private String extractRelevantLine(String raw) {
        if (raw.contains("Animation") && raw.contains("Partial")) {
            Matcher matcher = Pattern.compile("Animation (\\d+)").matcher(raw);
            if (matcher.find()) {
                return "Animation " + matcher.group(1) + " loaded";
            }
        }
        if (raw.matches(".*Animation (\\d+):.*?(\\d+)%\\|.*")) {
            Matcher matcher = Pattern.compile("Animation (\\d+):.*?(\\d+)%\\|").matcher(raw);
            if (matcher.find()) {
                return "Animation " + matcher.group(1) + " progress: " + matcher.group(2) + "%";
            }
        }
        if (raw.contains("File ready at")) {
            return "Final video ready!";
        } else if (raw.contains("Rendered ArchitectureDiagram")) {
            return "Rendering complete!";
        } else if (raw.contains("Played")) {
            return raw.trim();
        } else if (raw.contains("ERROR") || raw.contains("Exception")) {
            return "Error occurred during rendering";
        }
        if (raw.contains("/app/") || raw.contains("scene_file_writer") || raw.contains("scene.py")) {
            return null;
        }
        return null;
    }

}
