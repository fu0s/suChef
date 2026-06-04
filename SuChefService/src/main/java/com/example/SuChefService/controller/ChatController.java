package com.example.SuChefService.controller;

import com.example.SuChefService.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatService.streamChat(message);
    }

    @GetMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public String chat(@RequestParam String message) {
        return chatService.chat(message);
    }
}