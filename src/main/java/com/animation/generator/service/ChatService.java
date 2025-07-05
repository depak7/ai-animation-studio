package com.animation.generator.service;

import com.animation.generator.objects.Chats;
import com.animation.generator.objects.Diagram;
import com.animation.generator.repository.ChatRepository;
import com.animation.generator.repository.DiagramRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private DiagramRepository diagramRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private HttpServletRequest request;

    public List<Chats> getAllChatsofUserId() {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String guestId = (String) request.getAttribute("guestId");
            if (userId != null && userId > 0) {
                List<Chats> userChats = chatRepository.findChatsByUserId(userId);
                if (userChats != null) {
                    return userChats;
                }
            }
            if (guestId != null && !guestId.isEmpty()) {
                List<Chats> userChats = chatRepository.findChatsByGuestId(guestId);
                if (userChats != null) {
                    return userChats;
                }
            }
        } catch (Exception e) {
            log.error("Error in getAllChatsofUserId : {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<Diagram> getAllDiagramsofChatId(Long chatId) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String guestId = (String) request.getAttribute("guestId");
            if (userId != null && userId > 0 && chatId != null && chatId > 0) {
                List<Diagram> chatDiagrams = diagramRepository.findByUserIdAndChatId(userId, chatId);
                if (chatDiagrams != null) {
                    return chatDiagrams;
                }
            }
            if (guestId != null && !guestId.isEmpty() && chatId != null && chatId > 0) {
                List<Diagram> chatDiagrams = diagramRepository.findByGuestIdAndChatId(guestId, chatId);
                if (chatDiagrams != null) {
                    return chatDiagrams;
                }
            }
        } catch (Exception e) {
            log.error("Error in getAllDiagramsofChatId : {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public long createChat(JsonNode chatJson) {
        try {
            Chats chat = objectMapper.treeToValue(chatJson, Chats.class);
            chat.setCreatedAt(Instant.now());
            chat.setUpdatedAt(Instant.now());
            Chats savedChat = chatRepository.save(chat);
            return savedChat.getId();
        } catch (Exception e) {
            log.error("Error in createChat : {}", e.getMessage());
        }
        return -1;
    }

    public boolean checkIfChatExists(Long chatId) {
        return chatRepository.existsById(chatId);
    }

    public boolean deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
        return true;
    }
}
