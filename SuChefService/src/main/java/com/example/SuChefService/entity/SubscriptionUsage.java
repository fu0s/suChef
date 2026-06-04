package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "subscription_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SubscriptionUsage {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "month_year")
    private String monthYear;

    @Column(name = "current_documents_size_bytes")
    private Long currentDocumentsSizeBytes;

    @Column(name = "chats_count")
    private Integer chatsCount;

    @Column(name = "notifications_count")
    private Integer notificationsCount;
}
