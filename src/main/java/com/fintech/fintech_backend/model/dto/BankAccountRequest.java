package com.fintech.fintech_backend.model.dto;

import java.math.BigDecimal;

import com.fintech.fintech_backend.model.enums.AccountType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankAccountRequest {
    @NotNull
    private Long customerId;
    @NotNull
    private AccountType accountType;
    @Pattern(regexp = "^[A-Z]{3}$")
    @Builder.Default
    private String currency = "USD";
    @DecimalMin("0.00")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
