package com.banking.service;

import com.banking.dto.response.AdminDashboardResponse;
import com.banking.dto.response.CustomerResponse;
import com.banking.dto.response.TransactionResponse;
import com.banking.entity.Account;
import com.banking.entity.AuditLog;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import com.banking.exception.AccountNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    public AdminDashboardResponse getDashboardStats() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalAccounts(accountRepository.count())
                .activeAccounts(accountRepository.countByStatus(AccountStatus.ACTIVE))
                .frozenAccounts(accountRepository.countByStatus(AccountStatus.FROZEN))
                .totalTransactions(transactionRepository.count())
                .build();
    }

    public List<CustomerResponse> getAllCustomers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(CustomerResponse::from)
                .collect(Collectors.toList());
    }

    public void freezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.FROZEN);
        Account savedAccount = accountRepository.save(account);

        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log(
                adminEmail,
                "FREEZE_ACCOUNT",
                "Account",
                savedAccount.getId().toString(),
                "Account " + accountNumber + " was frozen by Admin"
        );
    }

    public void unfreezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.ACTIVE);
        Account savedAccount = accountRepository.save(account);

        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log(
                adminEmail,
                "UNFREEZE_ACCOUNT",
                "Account",
                savedAccount.getId().toString(),
                "Account " + accountNumber + " was unfrozen by Admin"
        );
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogService.getAll();
    }
}
