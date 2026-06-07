package com.example.SuChefService.document;

import com.example.SuChefService.entity.Restaurant;
import com.example.SuChefService.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBasedDocumentUploadValidator implements DocumentUploadValidator {

    private final SubscriptionService subscriptionService;

    @Override
    public void validate(Restaurant restaurant, long fileSize) {
        subscriptionService.checkDocumentLimit(restaurant, fileSize);
    }

    @Override
    public void trackUsage(Restaurant restaurant, long fileSize) {
        subscriptionService.incrementDocumentUsage(restaurant, fileSize);
    }

    @Override
    public boolean isUnlimited() {
        return false;
    }
}