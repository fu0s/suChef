package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vendors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {
    @Id
    private String id;
    private String name;

    @Column(name = "contact_info")
    private String contactInfo;
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
