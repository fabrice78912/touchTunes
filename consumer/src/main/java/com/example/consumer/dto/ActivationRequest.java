package com.example.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActivationRequest {
    private String serialNumber;
}

