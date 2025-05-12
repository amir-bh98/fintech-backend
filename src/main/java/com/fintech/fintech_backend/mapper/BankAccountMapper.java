package com.fintech.fintech_backend.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fintech.fintech_backend.model.BankAccount;
import com.fintech.fintech_backend.model.Customer;
import com.fintech.fintech_backend.model.dto.BankAccountRequest;
import com.fintech.fintech_backend.model.dto.BankAccountResponse;
import com.fintech.fintech_backend.model.enums.AccountStatus;

@Component
public class BankAccountMapper {

    public BankAccount mapToEntity(BankAccountRequest request, Customer customer) {
        return BankAccount.builder()
                .customer(customer)
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.Active)
                .build();
    }

    public BankAccountResponse mapToResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomer().getId())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
