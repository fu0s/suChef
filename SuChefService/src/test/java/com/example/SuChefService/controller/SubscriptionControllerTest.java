package com.example.SuChefService.controller;

import com.example.SuChefService.dto.RegisterRequest;
import com.example.SuChefService.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private RestaurantSubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionUsageRepository usageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        usageRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        restaurantRepository.deleteAll();

        if (planRepository.findById("free-plan-id").isEmpty()) {
            planRepository.save(com.example.SuChefService.entity.SubscriptionPlan.builder()
                    .id("free-plan-id")
                    .name("Free")
                    .price(java.math.BigDecimal.ZERO)
                    .maxDocumentsSizeMb(50L)
                    .maxChatsPerMonth(100)
                    .maxAccountsPerRestaurant(2)
                    .maxNotificationsPerMonth(10)
                    .build());
        }
    }

    private String getAuthToken() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Sub Test User")
                .email("subtest@example.com")
                .password("password")
                .restaurantName("Sub Test Restaurant")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }

    @Test
    void shouldGetCurrentSubscriptionSuccessfully() throws Exception {
        String token = getAuthToken();

        // This should trigger Jackson serialization of RestaurantSubscription -> SubscriptionPlan
        // and safely serialize the proxies because of @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
        mockMvc.perform(get("/api/subscription/current")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCurrentUsageSuccessfully() throws Exception {
        String token = getAuthToken();

        // This should trigger Jackson serialization of SubscriptionUsage -> Restaurant
        mockMvc.perform(get("/api/subscription/usage")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
