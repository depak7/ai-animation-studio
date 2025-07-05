package com.animation.generator.controllers;

import com.animation.generator.objects.User;
import com.animation.generator.repository.UserRepository;
import com.animation.generator.security.JwtUtil;
import com.animation.generator.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String googleClientId;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/google-authorize")
    public ResponseEntity<?> handleGoogleLogin(@RequestBody JsonNode payload) {
        try {
            JsonNode tokenNode = payload.get("googleToken");
            String guestId = payload.hasNonNull("guestId") ? payload.get("guestId").asText() : "";
            if (tokenNode == null || tokenNode.asText().isEmpty()) {
                return ResponseEntity.badRequest().body("Missing 'googleToken' in request");
            }
            String idTokenStr = tokenNode.asText();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(List.of(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenStr);
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google ID token");
            }
            GoogleIdToken.Payload userInfo = idToken.getPayload();
            String email = userInfo.getEmail();
            String name = (String) userInfo.get("name");
            User userExists = userService.checkUserExists(email);
            if (userExists == null) {
                JsonNode userJson = objectMapper.createObjectNode()
                        .put("email", email)
                        .put("name", name)
                        .put("guestId", guestId);

                userExists = userService.saveUser(userJson);
            }
            String jwt = jwtUtil.generateToken(userExists.getId(), false);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "userId", userExists.getId()
            ));
        } catch (Exception e) {
            log.error("Error during Google login:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }
}
