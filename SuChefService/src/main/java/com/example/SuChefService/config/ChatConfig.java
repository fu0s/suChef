package com.example.SuChefService.config;

import com.example.SuChefService.chat.ChatUsagePolicy;
import com.example.SuChefService.chat.SubscriptionBasedChatUsagePolicy;
import com.example.SuChefService.chat.UnlimitedChatUsagePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatConfig {

    @Value("${app.chat.unlimited-mode:false}")
    private boolean unlimitedMode;

    @Bean
    @Primary
    public ChatUsagePolicy chatUsagePolicy(SubscriptionBasedChatUsagePolicy subscriptionPolicy,
                                           UnlimitedChatUsagePolicy unlimitedPolicy) {
        return unlimitedMode ? unlimitedPolicy : subscriptionPolicy;
    }
}