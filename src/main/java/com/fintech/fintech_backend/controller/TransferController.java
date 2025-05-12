package com.fintech.fintech_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.fintech_backend.model.dto.TransferRequest;
import com.fintech.fintech_backend.model.dto.TransferResponse;
import com.fintech.fintech_backend.service.TransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transferFunds(
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.transferFunds(request));
    }

    @PutMapping("/{transferId}")
    public ResponseEntity<TransferResponse> updateTransfer(
            @PathVariable Long transferId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.updateTransfer(transferId, request));
    }

    @DeleteMapping("/{transferId}")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long transferId) {
        transferService.deleteTransfer(transferId);
        return ResponseEntity.noContent().build();
    }
}
