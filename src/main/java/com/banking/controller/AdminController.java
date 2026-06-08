package com.banking.controller;

import com.banking.dto.response.AdminDashboardResponse;
import com.banking.dto.response.CustomerResponse;
import com.banking.dto.response.TransactionResponse;
import com.banking.entity.AuditLog;
import com.banking.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Endpoints restricted to users with the ADMIN role")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get system statistics", description = "Retrieves aggregated counts of users, accounts, and transactions")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        AdminDashboardResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users/customers", description = "Retrieves a list of all registered users and their accounts")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> response = adminService.getAllCustomers();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/accounts/{accountNumber}/freeze")
    @Operation(summary = "Freeze a bank account", description = "Sets the status of an account to FROZEN")
    public ResponseEntity<Void> freezeAccount(@PathVariable String accountNumber) {
        adminService.freezeAccount(accountNumber);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accounts/{accountNumber}/unfreeze")
    @Operation(summary = "Unfreeze a bank account", description = "Restores the status of a frozen account to ACTIVE")
    public ResponseEntity<Void> unfreezeAccount(@PathVariable String accountNumber) {
        adminService.unfreezeAccount(accountNumber);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all transactions", description = "Retrieves a chronologically ordered list of all system transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> response = adminService.getAllTransactions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get all audit logs", description = "Retrieves a chronologically ordered list of all system audit records")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        List<AuditLog> response = adminService.getAuditLogs();
        return ResponseEntity.ok(response);
    }
}
