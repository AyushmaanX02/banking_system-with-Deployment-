package com.banking.controller;

import com.banking.dto.request.AccountRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a bank account", description = "Creates a new SAVINGS, CURRENT, or FIXED account for the authenticated user")
    public ResponseEntity<AccountResponse> createAccount(Authentication authentication, @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get user accounts", description = "Returns a list of accounts belonging to the authenticated user")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(Authentication authentication) {
        List<AccountResponse> response = accountService.getMyAccounts(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details by account number", description = "Returns account details if owned by the authenticated user")
    public ResponseEntity<AccountResponse> getAccount(Authentication authentication, @PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccount(accountNumber, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
