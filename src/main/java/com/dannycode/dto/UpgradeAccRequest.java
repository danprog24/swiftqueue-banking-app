package com.dannycode.dto;

import com.dannycode.model.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpgradeAccRequest {
    private AccountType accountType;
}