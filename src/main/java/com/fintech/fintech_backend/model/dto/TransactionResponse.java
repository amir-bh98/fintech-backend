package com.fintech.fintech_backend.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fintech.fintech_backend.model.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private Long linkedTransactionId;
    private LocalDateTime timestamp;
}
