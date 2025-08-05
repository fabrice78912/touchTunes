package com.example.common_lib.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExchangeResponse {

    private Meta meta;

    // La clé "XAF" ou toute autre monnaie => CurrencyData
    private Map<String, CurrencyData> data;

    @Data
    public static class Meta {
        @JsonProperty("last_updated_at")
        private String lastUpdatedAt;
    }

    @Data
    public static class CurrencyData {
        private String code;
        private Double value;
    }
}

