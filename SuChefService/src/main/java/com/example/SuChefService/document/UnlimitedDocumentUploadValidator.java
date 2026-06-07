package com.example.SuChefService.document;

import com.example.SuChefService.entity.Restaurant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UnlimitedDocumentUploadValidator implements DocumentUploadValidator {

    @Override
    public void validate(Restaurant restaurant, long fileSize) {
        // No limit in unlimited mode
        log.debug("Unlimited mode: skipping document size validation for restaurant {}", restaurant.getId());
    }

    @Override
    public void trackUsage(Restaurant restaurant, long fileSize) {
        // No usage tracking in unlimited mode
        log.debug("Unlimited mode: skipping document usage tracking for restaurant {}", restaurant.getId());
    }

    @Override
    public boolean isUnlimited() {
        return true;
    }
}