package com.example.SuChefService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Hello World API response.
 * This class encapsulates the response data returned by the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelloResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    private String status;

}
