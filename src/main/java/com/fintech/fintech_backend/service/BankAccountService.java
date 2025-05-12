package com.fintech.fintech_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fintech.fintech_backend.exception.ResourceNotFoundException;
import com.fintech.fintech_backend.mapper.BankAccountMapper;
import com.fintech.fintech_backend.model.BankAccount;
import com.fintech.fintech_backend.model.Customer;
import com.fintech.fintech_backend.model.dto.BankAccountRequest;
import com.fintech.fintech_backend.model.dto.BankAccountResponse;
import com.fintech.fintech_backend.model.enums.AccountStatus;
import com.fintech.fintech_backend.repository.BankAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerService customerService;
    private final BankAccountMapper bankAccountMapper;

    public BankAccountResponse createAccount(BankAccountRequest request) {
        Customer customer = customerService.getCustomerEntityById(request.getCustomerId());
        BankAccount newAccount = bankAccountMapper.mapToEntity(request, customer);
        BankAccount savedAccount = bankAccountRepository.save(newAccount);
        return bankAccountMapper.mapToResponse(savedAccount);
    }

    public BankAccountResponse getAccountById(Long id) {
        return bankAccountMapper.mapToResponse(
                bankAccountRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    // Internal method: Returns the entity (for service-layer use)
    public BankAccount getAccountEntityById(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
    }

    public List<BankAccountResponse> getAllAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(bankAccountMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    public BankAccountResponse updateAccount(Long id, BankAccountRequest request) {
        BankAccount existingAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        existingAccount.setAccountType(request.getAccountType());
        existingAccount.setCurrency(request.getCurrency());

        return bankAccountMapper.mapToResponse(bankAccountRepository.save(existingAccount));
    }

    public BankAccountResponse updateAccountStatus(Long id, AccountStatus status) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setStatus(status);
        return bankAccountMapper.mapToResponse(bankAccountRepository.save(account));
    }

    public void deleteAccount(Long id) {
        bankAccountRepository.deleteById(id);
    }
}
