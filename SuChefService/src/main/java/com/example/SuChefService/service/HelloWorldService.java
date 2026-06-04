package com.example.SuChefService.service;

import com.example.SuChefService.dto.HelloResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service layer for Hello World functionality.
 * This class contains the business logic for the hello world feature.
 * Following the separation of concerns principle.
 */
@Service
public class HelloWorldService {

    /**
     * Generates a hello world response with current timestamp.
     *
     * @return HelloResponse containing the greeting message and metadata
     */
    public HelloResponse getHelloMessage() {
        return HelloResponse.builder()
                .message("Hello, World!")
                .timestamp(LocalDateTime.now())
                .status("success")
                .build();
    }

    /**
     * Generates a personalized hello message.
     *
     * @param name the name to greet
     * @return HelloResponse containing the personalized greeting
     */
    public HelloResponse getHelloMessage(String name) {
        return HelloResponse.builder()
                .message("Hello, " + name + "!")
                .timestamp(LocalDateTime.now())
                .status("success")
                .build();
    }

}
