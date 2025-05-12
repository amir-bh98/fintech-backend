package com.fintech.fintech_backend.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fintech.fintech_backend.model.enums.AccountStatus;
import com.fintech.fintech_backend.model.enums.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BankAccountResponse {
    private Long id;
    private Long customerId;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;
}
