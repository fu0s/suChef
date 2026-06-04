package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Document {
    @Id
    private String id;
    private String name;
    private String type;
    private Long size;
    private LocalDateTime date;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING)
    private DocumentClassification classification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
