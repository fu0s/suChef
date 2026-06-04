package com.example.SuChefService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String id;
    private String name;
    private String type;
    private Long size;
    private LocalDateTime date;
    private LocalDateTime uploadedAt;
    private String status;
    private String classification;
}
