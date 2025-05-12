package com.fintech.fintech_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fintech.fintech_backend.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find transactions by linked transaction (for transfers)
    Optional<Transaction> findByLinkedTransactionId(Long linkedTransactionId);

    List<Transaction> findByAccountId(Long accountId);

}
