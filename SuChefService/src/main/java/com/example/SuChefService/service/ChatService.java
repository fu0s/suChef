package com.example.SuChefService.service;

import com.example.SuChefService.chat.IntentClassifier;
import com.example.SuChefService.chat.QueryIntent;
import com.example.SuChefService.chat.QueryRouter;
import com.example.SuChefService.entity.Restaurant;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final SubscriptionService subscriptionService;
    private final IntentClassifier intentClassifier;
    private final QueryRouter queryRouter;
    private final ObjectMapper objectMapper;

    public ChatService(ChatClient chatClient, SubscriptionService subscriptionService,
                       IntentClassifier intentClassifier, QueryRouter queryRouter,
                       ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.subscriptionService = subscriptionService;
        this.intentClassifier = intentClassifier;
        this.queryRouter = queryRouter;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("null")
    public String chat(String message) {
        Restaurant restaurant = subscriptionService.getCurrentRestaurant();
        subscriptionService.incrementChatUsage(restaurant);

        IntentClassifier.ClassifiedIntent intent = intentClassifier.classify(message);
        log.info("Intent: {} for message: {}", intent.intent(), message);

        String systemPrompt;
        String userMessage;

        if (intent.intent() != QueryIntent.GENERAL_QUESTION) {
            QueryRouter.QueryResult result = queryRouter.execute(intent);
            String dataJson;
            try {
                dataJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.data());
            } catch (Exception e) {
                dataJson = result.data().toString();
            }

            systemPrompt = "You are the SuChef AI Assistant. You have been given database data to answer the user's question. "
                    + "Use ONLY the data provided. Be concise and specific. Format numbers clearly. "
                    + "NEVER fabricate data. If the data is empty, say so.";
            userMessage = "User question: " + message + "\n\nData from database:\n" + dataJson;
        } else {
            systemPrompt = "You are the SuChef AI Assistant. You are a helpful restaurant management assistant. "
                    + "Answer general questions. Be concise and helpful.";
            userMessage = message;
        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    @SuppressWarnings("null")
    public Flux<String> streamChat(String message) {
        log.debug("Starting streamChat for message: {}", message);

        Restaurant restaurant = subscriptionService.getCurrentRestaurant();
        subscriptionService.checkChatLimit(restaurant);
        subscriptionService.incrementChatUsage(restaurant);

        IntentClassifier.ClassifiedIntent intent = intentClassifier.classify(message);
        log.info("Stream intent: {} for message: {}", intent.intent(), message);

        String systemPrompt;
        String userMessage;

        if (intent.intent() != QueryIntent.GENERAL_QUESTION) {
            QueryRouter.QueryResult result = queryRouter.execute(intent);
            String dataJson;
            try {
                dataJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.data());
            } catch (Exception e) {
                dataJson = result.data().toString();
            }

            systemPrompt = "You are the SuChef AI Assistant. You have been given database data to answer the user's question. "
                    + "Use ONLY the data provided. Be concise and specific. Format numbers clearly. "
                    + "NEVER fabricate data. If the data is empty, say so.";
            userMessage = "User question: " + message + "\n\nData from database:\n" + dataJson;
        } else {
            systemPrompt = "You are the SuChef AI Assistant. You are a helpful restaurant management assistant. "
                    + "Answer general questions. Be concise and helpful.";
            userMessage = message;
        }

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
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
