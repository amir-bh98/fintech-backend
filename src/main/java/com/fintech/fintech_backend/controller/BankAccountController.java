package com.fintech.fintech_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.fintech_backend.model.dto.BankAccountRequest;
import com.fintech.fintech_backend.model.dto.BankAccountResponse;
import com.fintech.fintech_backend.model.enums.AccountStatus;
import com.fintech.fintech_backend.service.BankAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    // Create Bank Account
    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            @Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse response = bankAccountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Single Account
    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(bankAccountService.getAccountById(id));
    }

    // Get All Accounts
    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(bankAccountService.getAllAccounts());
    }

    // Update Account
    @PutMapping("/{id}")
    public ResponseEntity<BankAccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody BankAccountRequest request) {
        return ResponseEntity.ok(bankAccountService.updateAccount(id, request));
    }

    // Delete Account
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        bankAccountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    // Update Account Status
    @PatchMapping("/{id}/status")
    public ResponseEntity<BankAccountResponse> updateAccountStatus(
            @PathVariable Long id,
            @RequestParam AccountStatus status) {
        return ResponseEntity.ok(bankAccountService.updateAccountStatus(id, status));
    }
}