package com.fintech.fintech_backend.mapper;

import org.springframework.stereotype.Component;

import com.fintech.fintech_backend.model.Transaction;
import com.fintech.fintech_backend.model.dto.TransferResponse;

@Component
public class TransferMapper {

    public TransferResponse mapToResponse(Transaction withdrawal, Transaction deposit) {
        return TransferResponse.builder()
                .status("COMPLETED")
                .message("Transfer successful")
                .withdrawalTransactionId(withdrawal.getId())
                .depositTransactionId(deposit.getId())
                .timestamp(withdrawal.getTimestamp())
                .build();
    }
}
