package com.animation.generator.service;

import com.animation.generator.objects.Chats;
import com.animation.generator.objects.Diagram;
import com.animation.generator.objects.User;
import com.animation.generator.repository.ChatRepository;
import com.animation.generator.repository.DiagramRepository;
import com.animation.generator.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiagramRepository diagramRepository;

    @Autowired
    private ChatRepository chatRepository;

    public User saveUser(JsonNode userJson) {
        try {
            if (userJson == null || !userJson.hasNonNull("email") || !userJson.hasNonNull("name") || !userJson.hasNonNull("guestId")) {
                log.warn("Missing required fields in userJson: {}", userJson);
                return null;
            }
            String email = userJson.get("email").asText();
            String name = userJson.get("name").asText();
            String guestId = userJson.get("guestId").asText();
            Optional<User> optionalUser = Optional.ofNullable(userRepository.findByEmail(email));
            User user = optionalUser.orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name);
                newUser.setGuestId(guestId);
                return userRepository.save(newUser);
            });
            boolean migrated = migrateGuestUserRecords(guestId, user.getId());
            if (!migrated) {
                log.warn("Guest records migration failed for guestId: {}", guestId);
            }
            return user;
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage(), e);
        }
        return null;
    }

    public boolean migrateGuestUserRecords(String guestId, Long userId) {
        if (guestId == null || guestId.isBlank() || userId == null || userId <= 0) {
            log.warn("Invalid guestId or userId provided for migration: guestId={}, userId={}", guestId, userId);
            return false;
        }
        try {
            List<Diagram> diagrams = diagramRepository.findByGuestId(guestId);
            for (Diagram diagram : diagrams) {
                diagram.setUserId(userId);
                diagram.setGuestId(guestId + userId);
            }
            diagramRepository.saveAll(diagrams);

            List<Chats> chats = chatRepository.findChatsByGuestId(guestId);
            for (Chats chat : chats) {
                chat.setUserId(userId);
                chat.setGuestId(guestId + userId);
            }
            chatRepository.saveAll(chats);

            log.info("Migrated {} diagrams and {} chats from guestId={} to userId={}", diagrams.size(), chats.size(), guestId, userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to migrate guest records: {}", e.getMessage(), e);
            return false;
        }
    }

    public User checkUserExists(String userEmail) {
        try {
            if (userEmail == null || userEmail.isEmpty()) {
                return null;
            }
            return userRepository.findByEmail(userEmail);
        } catch (Exception e) {
            log.error("Error checking user: {}", e.getMessage(), e);
        }
        return null;
    }

}
