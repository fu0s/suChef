package com.example.SuChefService.service;

import com.example.SuChefService.dto.HelloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HelloWorldService.
 * Tests the business logic layer without external dependencies.
 */
@SpringBootTest
@DisplayName("HelloWorldService Tests")
class HelloWorldServiceTests {

    @Autowired
    private HelloWorldService helloWorldService;

    @Test
    @DisplayName("Should return hello world message with current timestamp")
    void testGetHelloMessage() {
        // Act
        HelloResponse response = helloWorldService.getHelloMessage();

        // Assert
        assertNotNull(response);
        assertEquals("Hello, World!", response.getMessage());
        assertEquals("success", response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Should return personalized greeting")
    void testGetHelloMessageWithName() {
        // Act
        HelloResponse response = helloWorldService.getHelloMessage("Alice");

        // Assert
        assertNotNull(response);
        assertEquals("Hello, Alice!", response.getMessage());
        assertEquals("success", response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Should handle empty name parameter")
    void testGetHelloMessageWithEmptyName() {
        // Act
        HelloResponse response = helloWorldService.getHelloMessage("");

        // Assert
        assertNotNull(response);
        assertEquals("Hello, !", response.getMessage());
        assertEquals("success", response.getStatus());
    }

}
