package com.example.producer.model.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class JukeboxRequest {
    private String serialNumber;
    private String model;
    private String location;
}
