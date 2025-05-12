package com.fintech.fintech_backend.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fintech.fintech_backend.exception.DuplicateRequestException;

@Service
public class IdempotencyService {
    private final Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    public void checkIdempotency(String key) {
        if (processedKeys.contains(key)) {
            throw new DuplicateRequestException("Duplicate transaction request");
        }
        processedKeys.add(key);
    }
}
