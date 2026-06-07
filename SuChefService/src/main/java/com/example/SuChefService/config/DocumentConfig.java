package com.example.SuChefService.config;

import com.example.SuChefService.document.DocumentUploadValidator;
import com.example.SuChefService.document.SubscriptionBasedDocumentUploadValidator;
import com.example.SuChefService.document.UnlimitedDocumentUploadValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DocumentConfig {

    @Value("${app.document.unlimited-mode:false}")
    private boolean unlimitedMode;

    @Bean
    @Primary
    public DocumentUploadValidator documentUploadValidator(SubscriptionBasedDocumentUploadValidator subscriptionValidator,
                                                           UnlimitedDocumentUploadValidator unlimitedValidator) {
        return unlimitedMode ? unlimitedValidator : subscriptionValidator;
    }
}