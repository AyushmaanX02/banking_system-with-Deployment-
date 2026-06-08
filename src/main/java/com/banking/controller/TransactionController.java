package com.banking.controller;

import com.banking.dto.request.DepositRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.response.TransactionResponse;
import com.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for bank operations like deposit, withdrawal, transfer, and transaction logs")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds", description = "Deposit money into an active owned account")
    public ResponseEntity<TransactionResponse> deposit(Authentication authentication, @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds", description = "Withdraw money from an active owned account")
    public ResponseEntity<TransactionResponse> withdraw(Authentication authentication, @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.withdraw(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfer money from an owned active account to another active account")
    public ResponseEntity<TransactionResponse> transfer(Authentication authentication, @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountNumber}")
    @Operation(summary = "Get transaction history", description = "Retrieve transactions sorted by date descending for a specific owned account")
    public ResponseEntity<List<TransactionResponse>> getHistory(Authentication authentication, @PathVariable String accountNumber) {
        List<TransactionResponse> response = transactionService.getHistory(accountNumber, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
