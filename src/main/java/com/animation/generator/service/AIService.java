package com.animation.generator.service;

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

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class AIService {

    @Autowired
    private DiagramRepository diagramRepository;
    @Value("${spring.llm.api.key}")
    private String apiKey;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ChatService chatService;
    @Autowired
    private HttpServletRequest request;

    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public JsonNode generateDiagramFromPrompt(UserRequest userRequest) {
        ObjectNode response = objectMapper.createObjectNode();
        try {
            Long userId = request.getAttribute("userId") != null ? (Long) request.getAttribute("userId") : 0;
            String guestId = (String) request.getAttribute("guestId");

            if (userRequest == null || userRequest.getPrompt() == null || userRequest.getPrompt().isEmpty() || userRequest.getConversationId() == null || userRequest.getConversationId().isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing or invalid request parameters.");
                return response;
            }

            String manimCode;
            JsonNode jsonData;

            if (userRequest.isSkipllmResponse()) {
                if (userRequest.getCustomCode() == null || userRequest.getCustomCode().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Custom Manim code is missing.");
                    return response;
                }
                manimCode = userRequest.getCustomCode();
                jsonData = objectMapper.createObjectNode();
            } else {
                String fullPrompt = buildPromptWithHistory(userRequest);
                String llmResponseJson = queryLLM(fullPrompt);
                JsonNode root = objectMapper.readTree(llmResponseJson);

                if (!root.has("manimCode") || !root.has("jsonData")) {
                    response.put("success", false);
                    response.put("message", "Invalid LLM response.");
                    return response;
                }

                manimCode = root.get("manimCode").asText();
                jsonData = root.get("jsonData");
            }
            if (!manimCode.contains("class ArchitectureDiagram(Scene):")) {
                response.put("success", false);
                response.put("message", "code does not contain required class.");
                return response;
            }
            String decoded = manimCode.replace("\\n", "\n");
            String videoUrl = renderWithPythonMicroservice(userRequest.getConversationId(), decoded, jsonData);
            if (videoUrl == null || videoUrl.isEmpty()) {
                response.put("success", false);
                response.put("message", "Video rendering failed.");
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

            Diagram diagram = new Diagram();
            diagram.setUserId(userId);
            diagram.setGuestId(guestId);
            diagram.setPrompt(userRequest.getPrompt());
            diagram.setGeneratedCode(manimCode);
            diagram.setJsonRepresentation(jsonData.toPrettyString());
            diagram.setChatId(userRequest.getChatId());
            diagram.setVideoSource(videoUrl);
            diagramRepository.save(diagram);

            response.put("success", true);
            response.set("diagram", objectMapper.valueToTree(diagram));
            return response;

        } catch (Exception e) {
            log.error("Error generating diagram", e);
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
            JsonNode jsonNode = objectMapper.readTree(d.getJsonRepresentation());
            String summary = jsonNode.path("jsonData").path("summary").asText("");
            String style = jsonNode.path("jsonData").path("style").asText("");
            contextBuilder.append("Previous Prompt: ").append(d.getPrompt()).append("\n");
            contextBuilder.append("Previous Summary: ").append(summary).append("\n");
            contextBuilder.append("Previous Style: ").append(style).append("\n\n");
        }

        contextBuilder.append("User Prompt: ").append(userRequest.getPrompt()).append("\n");

        return """
                You are a Python developer who specializes in creating educational and visual animations using the Manim library (Community Edition).
                
                Your job is to take a high-level natural language prompt from a user and:
                1. Understand the core concept or scene being requested.
                2. Plan the animation logically using Manim constructs.
                3. Output a complete, clean Python script that builds the animation step-by-step using Manim CE.
                
                Your response must be a single JSON object with this structure:
                
                {
                  "jsonData": {
                    "summary": "<short summary of the scene concept>",
                    "style": "<visual style or layout used>",
                    ...
                  },
                  "manimCode": "<complete Python script>"
                }
                
                ### Requirements:
                - The "manimCode" should define a class `ArchitectureDiagram(Scene)`
                - Use only standard Manim CE classes like `Circle`, `Square`, `Text`, `Arrow`, `Line`, etc.
                - Use animation methods like `Create()`, `Write()`, `FadeIn()`, `Transform()`, etc.
                - Animate constructively — build scenes in logical steps using `self.wait()` between them
                - Do not output explanations, markdown, or commentary — return only the JSON
                
                ### Style Guidelines:
                - Layout should be clean, readable, and non-overlapping
                - Use consistent visual structure and labeling
                - Use appropriate shapes (e.g., triangle for geometry, rectangle for boxes, etc.)
                
                ### Context from recent user interactions (if applicable):
                %s
                
                Now generate the complete JSON + Manim code for the current user request.
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

    public String renderWithPythonMicroservice(String conversationId, String manimCode, JsonNode jsonData) throws IOException {
        ObjectNode requestJson = objectMapper.createObjectNode();
        requestJson.put("conversation_id", conversationId);
        requestJson.put("code", manimCode);
        requestJson.set("json_data", jsonData);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestJson), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.postForEntity("https://ai-animator-manim-runner.livelyocean-b0186b38.southindia.azurecontainerapps.io/run", requestEntity, JsonNode.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new IOException("Failed to call Python microservice: " + response.getStatusCode() + " - " + response.getBody());
        }
        return Objects.requireNonNull(response.getBody()).get("url").asText();
    }

}
