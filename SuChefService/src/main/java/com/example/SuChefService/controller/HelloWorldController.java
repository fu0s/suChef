package com.example.SuChefService.controller;

import com.example.SuChefService.dto.HelloResponse;
import com.example.SuChefService.service.HelloWorldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Hello World API endpoints.
 * Handles HTTP requests and returns JSON responses.
 * 
 * Base path: /api/v1/hello
 */
@RestController
@RequestMapping("/api/v1/hello")
@RequiredArgsConstructor
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    /**
     * GET endpoint that returns a simple hello world message.
     *
     * @return ResponseEntity containing HelloResponse with 200 OK status
     * 
     * Example: GET /api/v1/hello
     */
    @GetMapping
    public ResponseEntity<HelloResponse> sayHello() {
        HelloResponse response = helloWorldService.getHelloMessage();
        return ResponseEntity.ok(response);
    }

    /**
     * GET endpoint that returns a personalized hello message.
     * 
     * @param name the name to greet (optional, defaults to "World" if not provided)
     * @return ResponseEntity containing HelloResponse with 200 OK status
     * 
     * Example: GET /api/v1/hello?name=John
     */
    @GetMapping("/greet")
    public ResponseEntity<HelloResponse> greetByName(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        HelloResponse response = helloWorldService.getHelloMessage(name);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint.
     * Used to verify the service is running.
     *
     * @return ResponseEntity with 200 OK status and simple message
     * 
     * Example: GET /api/v1/hello/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }

}
