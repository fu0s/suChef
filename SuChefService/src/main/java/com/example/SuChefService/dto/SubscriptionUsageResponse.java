package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUsageResponse {
    private String id;
    private String monthYear;
    private Long currentDocumentsSizeBytes;
    private Integer chatsCount;
    private Integer notificationsCount;
}
