package com.fintech.fintech_backend.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String status;
    private String message;
    private Long withdrawalTransactionId;
    private Long depositTransactionId;
    private LocalDateTime timestamp;
}
