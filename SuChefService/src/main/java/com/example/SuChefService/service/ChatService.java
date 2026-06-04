package com.example.SuChefService.service;

import com.example.SuChefService.entity.Restaurant;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final SubscriptionService subscriptionService;

    public ChatService(ChatClient chatClient, SubscriptionService subscriptionService) {
        this.chatClient = chatClient;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Sends a message to the AI and returns the full response.
     * 
     * @param message The user's message.
     * @return The AI's response as a string.
     */
    @SuppressWarnings("null")
    public String chat(String message) {
        Restaurant restaurant = subscriptionService.getCurrentRestaurant();
        subscriptionService.incrementChatUsage(restaurant);

        return chatClient.prompt()
                .system("You are the SuChef AI Assistant. You MUST use the provided database tools to answer questions about orders, inventory, vendors, documents, and menu items. NEVER guess, invent, or fabricate data. If a tool returns an error or empty result, tell the user that the data could not be retrieved at this time. NEVER make up transactions, orders, or any business data.")
                .user(message)
                .call()
                .content();
    }

    /**
     * Sends a message to the AI and returns a stream of response tokens.
     * 
     * @param message The user's message.
     * @return A Flux of strings representing the streaming response.
     */
    @SuppressWarnings("null")
    public Flux<String> streamChat(String message) {
        log.debug("Starting streamChat for message: {}", message);

        Restaurant restaurant = subscriptionService.getCurrentRestaurant();
        subscriptionService.checkChatLimit(restaurant);
        // Note: For streaming, we increment on start.
        // In a more complex system, we might increment at the end or based on tokens.
        subscriptionService.incrementChatUsage(restaurant);

        return chatClient.prompt()

                .system("You are the SuChef AI Assistant. You MUST use the provided database tools to answer questions about orders, inventory, vendors, documents, and menu items. NEVER guess, invent, or fabricate data. If a tool returns an error or empty result, tell the user that the data could not be retrieved at this time. NEVER make up transactions, orders, or any business data.")
                .user(message)
                .stream()
                .content()
                .doOnNext(token -> log.trace("Emitting token: {}", token))
                .doOnComplete(() -> log.debug("streamChat completed"))
                .doOnError(e -> log.error("Error in streamChat: ", e))
                .onErrorResume(e -> {
                    log.warn("Resuming empty flux after error: {}", e.getMessage());
                    return Flux.empty();
                });
    }
}
