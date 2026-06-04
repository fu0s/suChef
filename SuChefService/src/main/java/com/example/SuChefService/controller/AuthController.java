package com.example.SuChefService.controller;

import com.example.SuChefService.dto.AuthRequest;
import com.example.SuChefService.dto.AuthResponse;
import com.example.SuChefService.dto.RegisterRequest;
import com.example.SuChefService.dto.SetRestaurantRequest;
import com.example.SuChefService.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String JWT_COOKIE_NAME = "suChef_jwt";
    private static final int COOKIE_MAX_AGE = 10 * 60 * 60; // 10 hours, matching JWT expiry

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = service.register(request);
        addJwtCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = service.authenticate(request);
        addJwtCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/restaurant")
    public ResponseEntity<AuthResponse> setRestaurant(
            @Valid @RequestBody SetRestaurantRequest request,
            java.security.Principal principal,
            HttpServletResponse response) {
        AuthResponse authResponse = service.setRestaurant(principal.getName(), request.getRestaurantName());
        addJwtCookie(response, authResponse.getToken());
        return ResponseEntity.ok(authResponse);
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(JWT_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/api")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
