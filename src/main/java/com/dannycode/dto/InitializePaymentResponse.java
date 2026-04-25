package com.dannycode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitializePaymentResponse {
    private boolean status;
    private String message;
    private Data data;

    @Getter
    @Setter
    public static class Data {
        @JsonProperty("authorization_url")
        private String authorizationUrl;

        @JsonProperty("access_code")
        private String accessCode;

        private String reference;
    }
}