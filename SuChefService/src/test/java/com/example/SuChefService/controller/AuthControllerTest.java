package com.example.SuChefService.controller;

import com.example.SuChefService.dto.AuthRequest;
import com.example.SuChefService.dto.RegisterRequest;
import com.example.SuChefService.repository.UserRepository;
import com.example.SuChefService.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private com.example.SuChefService.repository.RestaurantSubscriptionRepository subscriptionRepository;

    @Autowired
    private com.example.SuChefService.repository.SubscriptionUsageRepository usageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        usageRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        restaurantRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .restaurantName("Test Restaurant")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        // First register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .restaurantName("Test Restaurant")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Then login
        AuthRequest loginRequest = AuthRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldLinkToExistingRestaurantByName() throws Exception {
        RegisterRequest request1 = RegisterRequest.builder()
                .name("User One")
                .email("user1@example.com")
                .password("password")
                .restaurantName("Shared Restaurant")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        RegisterRequest request2 = RegisterRequest.builder()
                .name("User Two")
                .email("user2@example.com")
                .password("password")
                .restaurantName("Shared Restaurant")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(1, restaurantRepository.count());
    }

    @Test
    void shouldCreateSeparateRestaurantsForDifferentNames() throws Exception {
        RegisterRequest request1 = RegisterRequest.builder()
                .name("User One")
                .email("user1@example.com")
                .password("password")
                .restaurantName("Restaurant One")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        RegisterRequest request2 = RegisterRequest.builder()
                .name("User Two")
                .email("user2@example.com")
                .password("password")
                .restaurantName("Restaurant Two")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(2, restaurantRepository.count());
    }
}
