package com.example.SuChefService.exception;

public class NoRestaurantException extends RuntimeException {
    public NoRestaurantException(String message) {
        super(message);
    }
}
