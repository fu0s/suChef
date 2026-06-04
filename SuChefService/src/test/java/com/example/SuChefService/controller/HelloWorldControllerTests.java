package com.example.SuChefService.controller;

import com.example.SuChefService.dto.HelloResponse;
import com.example.SuChefService.service.HelloWorldService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for HelloWorldController.
 * Uses MockMvc to test REST endpoints without starting the full server.
 */
@SpringBootTest
@DisplayName("HelloWorldController Tests")
class HelloWorldControllerTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return hello world message")
    void testSayHello() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"))
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should return personalized greeting")
    void testGreetByName() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/hello/greet?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, John!"))
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Should return health check status")
    void testHealth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/hello/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsString("running")));
    }

}
