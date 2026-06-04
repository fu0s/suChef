package com.example.SuChefService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "restaurants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Restaurant {
    @Id
    private String id;
    private String name;
    private String location;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "contact_info")
    private String contactInfo;
}
