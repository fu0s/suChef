package com.example.SuChefService.document;

import com.example.SuChefService.entity.Restaurant;

public interface DocumentUploadValidator {
    
    void validate(Restaurant restaurant, long fileSize);
    
    void trackUsage(Restaurant restaurant, long fileSize);
    
    boolean isUnlimited();
}