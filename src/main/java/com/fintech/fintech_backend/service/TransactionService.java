package com.fintech.fintech_backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.fintech_backend.exception.IllegalOperationException;
import com.fintech.fintech_backend.exception.InsufficientBalanceException;
import com.fintech.fintech_backend.exception.ResourceNotFoundException;
import com.fintech.fintech_backend.mapper.TransactionMapper;
import com.fintech.fintech_backend.model.BankAccount;
import com.fintech.fintech_backend.model.Transaction;
import com.fintech.fintech_backend.model.dto.TransactionRequest;
import com.fintech.fintech_backend.model.dto.TransactionResponse;
import com.fintech.fintech_backend.model.enums.TransactionType;
import com.fintech.fintech_backend.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BankAccountService bankAccountService;
    private final TransactionMapper mapper;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        BankAccount account = bankAccountService.getAccountEntityById(request.getAccountId());

        Transaction transaction = mapper.mapToEntity(request, account);
        updateAccountBalance(account, transaction.getTransactionType(), transaction.getAmount());

        Transaction saved = transactionRepository.save(transaction);
        return mapper.mapToResponse(saved);
    }

    // Internal method: Returns the entity (for service-layer use)
    @Transactional
    public Transaction createTransaction(Long accountId, TransactionType transactionType, BigDecimal amount,
            Long linkedId) {
        BankAccount account = bankAccountService.getAccountEntityById(accountId);
        Transaction linkedTransaction = transactionRepository.findById(linkedId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + linkedId));

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(transactionType)
                .amount(amount)
                .linkedTransaction(linkedTransaction)
                .build();
        updateAccountBalance(account, transaction.getTransactionType(), transaction.getAmount());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void linkTransactions(Long withdrawalId, Long depositId) {
        Transaction withdrawal = getTransactionEntity(withdrawalId);
        Transaction deposit = getTransactionEntity(depositId);

        withdrawal.setLinkedTransaction(deposit);
        deposit.setLinkedTransaction(withdrawal);

        transactionRepository.saveAll(List.of(withdrawal, deposit));
    }

    public List<TransactionResponse> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId).stream()
                .map(mapper::mapToResponse)
                .collect(Collectors.toList());
    }

    // Internal method: Returns the entity (for service-layer use)
    public Transaction getTransactionEntity(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }

    @Transactional
    public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Prevent updates to transfer-linked transactions
        if (transaction.getLinkedTransaction() != null) {
            throw new IllegalOperationException("Cannot update transfer-linked transaction directly");
        }

        BankAccount account = bankAccountService.getAccountEntityById(request.getAccountId());

        // Reverse old transaction
        reverseAccountBalance(transaction);

        // Apply new transaction
        updateAccountBalance(account, request.getTransactionType(), request.getAmount());

        transaction.setAccount(account);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());

        Transaction updated = transactionRepository.save(transaction);
        return mapper.mapToResponse(updated);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        reverseAccountBalance(transaction);
        transactionRepository.delete(transaction);
    }

    private void updateAccountBalance(BankAccount account, TransactionType type, BigDecimal amount) {
        BigDecimal newBalance = type == TransactionType.Deposit
                ? account.getBalance().add(amount)
                : account.getBalance().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Insufficient funds");
        }
        account.setBalance(newBalance);
    }

    private void reverseAccountBalance(Transaction transaction) {
        BankAccount account = transaction.getAccount();
        BigDecimal reversalAmount = transaction.getTransactionType() == TransactionType.Deposit
                ? transaction.getAmount().negate()
                : transaction.getAmount();

        account.setBalance(account.getBalance().add(reversalAmount));
    }

    @Transactional
    public void reverseTransaction(Transaction transaction) {
        BankAccount account = transaction.getAccount();
        BigDecimal reversal = transaction.getTransactionType() == TransactionType.Deposit
                ? transaction.getAmount().negate()
                : transaction.getAmount();

        account.setBalance(account.getBalance().add(reversal));
    }

    @Transactional
    public void saveTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
