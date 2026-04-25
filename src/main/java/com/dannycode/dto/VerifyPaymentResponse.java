package com.dannycode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPaymentResponse {
    private boolean status;
    private String message;
    private Data data;

    @Getter
    @Setter
    public static class Data {
        private String status; // "success", "failed", "pending"
        private String reference;
        private Long amount; // in kobo
        @JsonProperty("gateway_response")
        private String gatewayResponse;
    }
}