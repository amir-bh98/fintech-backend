package com.fintech.fintech_backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.fintech_backend.exception.CurrencyMismatchException;
import com.fintech.fintech_backend.exception.InsufficientBalanceException;
import com.fintech.fintech_backend.model.BankAccount;
import com.fintech.fintech_backend.model.Transaction;
import com.fintech.fintech_backend.model.dto.TransactionRequest;
import com.fintech.fintech_backend.model.dto.TransactionResponse;
import com.fintech.fintech_backend.model.dto.TransferRequest;
import com.fintech.fintech_backend.model.dto.TransferResponse;
import com.fintech.fintech_backend.model.enums.TransactionType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransactionService transactionService;
    private final BankAccountService bankAccountService;
    private final IdempotencyService idempotencyService;

    @Transactional
    public TransferResponse transferFunds(TransferRequest request) {
        idempotencyService.checkIdempotency(request.getIdempotencyKey());

        BankAccount source = bankAccountService.getAccountEntityById(request.getSourceAccountId());
        BankAccount dest = bankAccountService.getAccountEntityById(request.getDestinationAccountId());

        validateTransfer(source, dest, request.getAmount());

        // Create withdrawal
        TransactionResponse withdrawal = transactionService.createTransaction(
                new TransactionRequest(
                        request.getSourceAccountId(),
                        TransactionType.Withdraw,
                        request.getAmount(),
                        null));

        // Create deposit
        TransactionResponse deposit = transactionService.createTransaction(
                new TransactionRequest(
                        request.getDestinationAccountId(),
                        TransactionType.Deposit,
                        request.getAmount(),
                        withdrawal.getId()));

        // Link transactions
        transactionService.linkTransactions(withdrawal.getId(), deposit.getId());

        return TransferResponse.builder()
                .status("COMPLETED")
                .withdrawalTransactionId(withdrawal.getId())
                .depositTransactionId(deposit.getId())
                .timestamp(withdrawal.getTimestamp())
                .build();
    }

    private void validateTransfer(BankAccount source, BankAccount dest, BigDecimal amount) {
        if (source.getId().equals(dest.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        if (!source.getCurrency().equals(dest.getCurrency())) {
            throw new CurrencyMismatchException(
                    "Currency mismatch: " + source.getCurrency() + " vs " + dest.getCurrency());
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + source.getId());
        }
    }

    @Transactional
    public void deleteTransfer(Long withdrawalId) {
        Transaction withdrawal = transactionService.getTransactionEntity(withdrawalId);
        Transaction deposit = withdrawal.getLinkedTransaction();

        // Reverse balances
        transactionService.reverseTransaction(withdrawal);
        transactionService.reverseTransaction(deposit);

        // Delete transactions
        transactionService.deleteTransaction(withdrawal.getId());
        transactionService.deleteTransaction(deposit.getId());
    }

    @Transactional
    public TransferResponse updateTransfer(Long withdrawalId, TransferRequest request) {
        Transaction oldWithdrawal = transactionService.getTransactionEntity(withdrawalId);
        Transaction oldDeposit = oldWithdrawal.getLinkedTransaction();

        boolean sameSource = oldWithdrawal.getAccount().getId().equals(request.getSourceAccountId());
        boolean sameDest = oldDeposit.getAccount().getId().equals(request.getDestinationAccountId());

        if (sameSource && sameDest) {
            return handleSameAccountsUpdate(oldWithdrawal, oldDeposit, request);
        } else if (sameSource) {
            return handleNewDestination(oldWithdrawal, oldDeposit, request);
        } else if (sameDest) {
            return handleNewSource(oldWithdrawal, oldDeposit, request);
        } else {
            return handleNewTransfer(oldWithdrawal, oldDeposit, request);
        }
    }

    private TransferResponse handleSameAccountsUpdate(Transaction withdrawal,
            Transaction deposit,
            TransferRequest request) {
        BigDecimal oldAmount = withdrawal.getAmount();
        BigDecimal newAmount = request.getAmount();

        // Update withdrawal
        withdrawal.setAmount(newAmount);
        updateAccountBalance(withdrawal.getAccount(), TransactionType.Withdraw, oldAmount, newAmount);

        // Update deposit
        deposit.setAmount(newAmount);
        updateAccountBalance(deposit.getAccount(), TransactionType.Deposit, oldAmount, newAmount);

        transactionService.saveTransaction(withdrawal);
        transactionService.saveTransaction(deposit);

        return buildResponse(withdrawal, deposit);
    }

    private TransferResponse handleNewDestination(Transaction withdrawal,
            Transaction oldDeposit,
            TransferRequest request) {
        // Reverse old deposit
        transactionService.reverseTransaction(oldDeposit);
        transactionService.deleteTransaction(oldDeposit.getId());

        // Create new deposit
        Transaction newDeposit = createDeposit(
                request.getDestinationAccountId(),
                request.getAmount(),
                withdrawal.getId());

        // Update withdrawal amount if changed
        if (!withdrawal.getAmount().equals(request.getAmount())) {
            updateWithdrawalAmount(withdrawal, request.getAmount());
        }

        return buildResponse(withdrawal, newDeposit);
    }

    private TransferResponse handleNewSource(Transaction oldWithdrawal,
            Transaction deposit,
            TransferRequest request) {
        // Reverse old withdrawal
        transactionService.reverseTransaction(oldWithdrawal);
        transactionService.deleteTransaction(oldWithdrawal.getId());

        // Create new withdrawal
        Transaction newWithdrawal = createWithdrawal(
                request.getSourceAccountId(),
                request.getAmount(),
                deposit.getId());

        // Update deposit amount if changed
        if (!deposit.getAmount().equals(request.getAmount())) {
            updateDepositAmount(deposit, request.getAmount());
        }

        return buildResponse(newWithdrawal, deposit);
    }

    private TransferResponse handleNewTransfer(Transaction oldWithdrawal,
            Transaction oldDeposit,
            TransferRequest request) {
        // Delete old transfer
        deleteTransfer(oldWithdrawal.getId());

        // Create new transfer
        return transferFunds(request);
    }

    private Transaction createWithdrawal(Long accountId, BigDecimal amount, Long linkedId) {
        return transactionService.createTransaction(accountId, TransactionType.Withdraw, amount,
                linkedId);
    }

    private Transaction createDeposit(Long accountId, BigDecimal amount, Long linkedId) {
        return transactionService.createTransaction(accountId, TransactionType.Deposit, amount, linkedId);
    }

    private Transaction updateWithdrawalAmount(Transaction withdrawal, BigDecimal amount) {
        withdrawal.setAmount(amount);
        return transactionService.updateTransaction(withdrawal);
    }

    private Transaction updateDepositAmount(Transaction deposit, BigDecimal amount) {
        deposit.setAmount(amount);
        return transactionService.updateTransaction(deposit);
    }

    private void updateAccountBalance(BankAccount account, TransactionType type,
            BigDecimal oldAmount, BigDecimal newAmount) {
        BigDecimal difference = type == TransactionType.Deposit
                ? newAmount.subtract(oldAmount)
                : oldAmount.subtract(newAmount);

        account.setBalance(account.getBalance().add(difference));
    }

    private TransferResponse buildResponse(Transaction withdrawal, Transaction deposit) {
        return TransferResponse.builder()
                .status("UPDATED")
                .withdrawalTransactionId(withdrawal.getId())
                .depositTransactionId(deposit.getId())
                .timestamp(withdrawal.getTimestamp())
                .build();
    }

}