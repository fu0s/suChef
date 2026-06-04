package com.example.SuChefService.service;

import com.example.SuChefService.dto.AuthRequest;
import com.example.SuChefService.dto.AuthResponse;
import com.example.SuChefService.dto.RegisterRequest;
import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.entity.User;
import com.example.SuChefService.exception.ResourceNotFoundException;
import com.example.SuChefService.exception.SubscriptionLimitExceededException;
import com.example.SuChefService.repository.RestaurantRepository;
import com.example.SuChefService.repository.UserRepository;
import com.example.SuChefService.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private AuthService authService;

    private Restaurant existingRestaurant;

    @BeforeEach
    void setUp() {
        existingRestaurant = Restaurant.builder()
                .id("rest-123")
                .name("Test Restaurant")
                .build();
    }

    @Test
    void register_withNewRestaurant_shouldCreateRestaurantAndUser() {
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("plainPassword")
                .restaurantName("New Restaurant")
                .build();

        when(restaurantRepository.findByName("New Restaurant")).thenReturn(Optional.empty());
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("New Restaurant", response.getRestaurantName());

        verify(restaurantRepository).findByName("New Restaurant");
        verify(restaurantRepository).save(any(Restaurant.class));
        verify(subscriptionService).checkAccountLimit(any(Restaurant.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withExistingRestaurant_shouldLinkToExistingRestaurant() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("plainPassword")
                .restaurantName("Test Restaurant")
                .build();

        when(restaurantRepository.findByName("Test Restaurant")).thenReturn(Optional.of(existingRestaurant));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("jane@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("Test Restaurant", response.getRestaurantName());

        verify(restaurantRepository).findByName("Test Restaurant");
        verify(restaurantRepository, never()).save(any(Restaurant.class));
        verify(subscriptionService).checkAccountLimit(existingRestaurant);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenAccountLimitExceeded() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Limit User")
                .email("limit@example.com")
                .password("plainPassword")
                .restaurantName("Test Restaurant")
                .build();

        when(restaurantRepository.findByName("Test Restaurant")).thenReturn(Optional.of(existingRestaurant));
        doThrow(new SubscriptionLimitExceededException("Account limit reached"))
                .when(subscriptionService).checkAccountLimit(existingRestaurant);

        assertThrows(SubscriptionLimitExceededException.class, () -> authService.register(request));

        verify(restaurantRepository).findByName("Test Restaurant");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_shouldReturnAuthResponse_onSuccessfulLogin() {
        AuthRequest request = AuthRequest.builder()
                .email("john@example.com")
                .password("password")
                .build();

        User user = User.builder()
                .id("user-123")
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .restaurant(existingRestaurant)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("Test Restaurant", response.getRestaurantName());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void setRestaurant_shouldUpdateRestaurantAndReturnAuthResponse() {
        User user = User.builder()
                .id("user-123")
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(restaurantRepository.findByName("Updated Restaurant")).thenReturn(Optional.empty());
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.setRestaurant("john@example.com", "Updated Restaurant");

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Updated Restaurant", response.getRestaurantName());
        assertEquals("Updated Restaurant", user.getRestaurant().getName());

        verify(userRepository).findByEmail("john@example.com");
        verify(restaurantRepository).findByName("Updated Restaurant");
        verify(restaurantRepository).save(any(Restaurant.class));
        verify(userRepository).save(user);
    }
}
