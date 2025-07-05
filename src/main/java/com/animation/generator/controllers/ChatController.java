package com.animation.generator.controllers;

import com.animation.generator.objects.Chats;
import com.animation.generator.objects.Diagram;
import com.animation.generator.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/get-all-chats-of-user")
    public List<Chats> getAllChatsofUser() {
        return chatService.getAllChatsofUserId();
    }

    @GetMapping("/get-chat-history")
    public List<Diagram> getChatHistory(@RequestParam Long chatId) {
        return chatService.getAllDiagramsofChatId(chatId);
    }

    @GetMapping("/chats-exists")
    public boolean chatsExist(@RequestParam Long chatId) {
        return chatService.checkIfChatExists(chatId);
    }

    @DeleteMapping("/delete-chats")
    public boolean deleteChats(@RequestParam Long chatId) {
        return chatService.deleteChat(chatId);
    }

}
