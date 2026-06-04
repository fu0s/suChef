package com.example.SuChefService.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SetRestaurantRequest {
    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;
}
