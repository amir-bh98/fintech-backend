package com.fintech.fintech_backend.mapper;

import org.springframework.stereotype.Component;

import com.fintech.fintech_backend.model.BankAccount;
import com.fintech.fintech_backend.model.Transaction;
import com.fintech.fintech_backend.model.dto.TransactionRequest;
import com.fintech.fintech_backend.model.dto.TransactionResponse;

@Component
public class TransactionMapper {

    public Transaction mapToEntity(TransactionRequest request, BankAccount account) {
        return Transaction.builder()
                .account(account)
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .build();
    }

    public TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .linkedTransactionId(
                        transaction.getLinkedTransaction() != null ? transaction.getLinkedTransaction().getId() : null)
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
