package com.dannycode.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InitializePaymentRequest {
    private String email;
    private Long amount;
    private String reference;
    private String callbackUrl;
}