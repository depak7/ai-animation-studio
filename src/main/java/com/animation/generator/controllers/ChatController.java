package com.animation.generator.controllers;

import com.animation.generator.objects.Chats;
import com.animation.generator.objects.Diagram;
import com.animation.generator.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/get-all-chats-of-user")
    public List<Chats> getAllChatsofUser(@RequestParam Long userId) {
        return chatService.getAllChatsofUserId(userId);
    }

    @GetMapping("/get-chat-history")
    public List<Diagram> getChatHistory(@RequestParam Long userId, @RequestParam Long chatId) {
        return chatService.getAllDiagramsofChatId(userId, chatId);
    }

    @GetMapping("/chats-exists")
    public boolean chatsExist(@RequestParam Long userId, @RequestParam Long chatId) {
        return chatService.checkIfChatExists(userId);
    }

    @DeleteMapping("/delete-chats")
    public boolean deleteChats(@RequestParam Long chatId) {
        return chatService.deleteChat(chatId);
    }

}
