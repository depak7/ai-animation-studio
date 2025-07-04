package com.animation.generator.service;

import com.animation.generator.objects.Chats;
import com.animation.generator.objects.Diagram;
import com.animation.generator.repository.ChatRepository;
import com.animation.generator.repository.DiagramRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public List<Chats> getAllChatsofUserId(Long userId) {
        try {
            if (userId != null && userId > 0) {
                List<Chats> userChats = chatRepository.findChatsByUserId(userId);
                if (userChats != null) {
                    return userChats;
                }
            }
        } catch (Exception e) {
            log.error("Error in getAllChatsofUserId : {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<Diagram> getAllDiagramsofChatId(Long userId, Long chatId) {
        try {
            if (userId != null && userId > 0 && chatId != null && chatId > 0) {
                List<Diagram> chatDiagrams = diagramRepository.findByUserIdAndChatId(userId, chatId);
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
