package com.banking.service;

import com.banking.dto.request.DepositRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.response.TransactionResponse;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.enums.AccountStatus;
import com.banking.enums.TransactionType;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.UnauthorisedAccessException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    public TransactionResponse deposit(String email, DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getAccountNumber()));

        // Verify ownership
        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorisedAccessException("You are not authorized to deposit into this account");
        }

        // Check active status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorisedAccessException("Account status is " + account.getStatus() + ". Only active accounts can perform deposits.");
        }

        // Add amount
        account.setBalance(account.getBalance().add(request.getAmount()));
        Account savedAccount = accountRepository.save(account);

        // Build and save transaction
        Transaction transaction = Transaction.builder()
                .referenceNumber("DEP" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(savedAccount.getBalance())
                .description(request.getDescription())
                .account(savedAccount)
                .build();

        Transaction savedTxn = transactionRepository.save(transaction);

        // Audit Log
        auditLogService.log(
                email,
                "DEPOSIT",
                "Account",
                savedAccount.getId().toString(),
                "Deposited " + request.getAmount() + " to account " + savedAccount.getAccountNumber()
        );

        return TransactionResponse.from(savedTxn);
    }

    public TransactionResponse withdraw(String email, DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getAccountNumber()));

        // Verify ownership
        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorisedAccessException("You are not authorized to withdraw from this account");
        }

        // Check active status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorisedAccessException("Account status is " + account.getStatus() + ". Only active accounts can perform withdrawals.");
        }

        // Check balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + account.getAccountNumber() + ". Current balance: " + account.getBalance());
        }

        // Subtract amount
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        Account savedAccount = accountRepository.save(account);

        // Build and save transaction
        Transaction transaction = Transaction.builder()
                .referenceNumber("WTH" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(savedAccount.getBalance())
                .description(request.getDescription())
                .account(savedAccount)
                .build();

        Transaction savedTxn = transactionRepository.save(transaction);

        // Audit Log
        auditLogService.log(
                email,
                "WITHDRAWAL",
                "Account",
                savedAccount.getId().toString(),
                "Withdrew " + request.getAmount() + " from account " + savedAccount.getAccountNumber()
        );

        return TransactionResponse.from(savedTxn);
    }

    public TransactionResponse transfer(String email, TransferRequest request) {
        // Same-account guard
        if (request.getFromAccountNumber().trim().equals(request.getToAccountNumber().trim())) {
            throw new UnauthorisedAccessException("Cannot transfer to the same account");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found: " + request.getFromAccountNumber()));

        Account destAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found: " + request.getToAccountNumber()));

        // Verify ownership of source
        if (!sourceAccount.getUser().getEmail().equals(email)) {
            throw new UnauthorisedAccessException("You are not authorized to transfer from this account");
        }

        // Check both ACTIVE
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorisedAccessException("Source account status is " + sourceAccount.getStatus() + ". Only active accounts can transfer funds.");
        }
        if (destAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorisedAccessException("Destination account status is " + destAccount.getStatus() + ". Only active accounts can receive funds.");
        }

        // Check sufficient balance
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + sourceAccount.getAccountNumber() + ". Current balance: " + sourceAccount.getBalance());
        }

        // Update balances
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destAccount.setBalance(destAccount.getBalance().add(request.getAmount()));

        Account savedSource = accountRepository.save(sourceAccount);
        accountRepository.save(destAccount);

        // Save one Transaction on source account with toAccountNumber set
        Transaction transaction = Transaction.builder()
                .referenceNumber("TRF" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                .transactionType(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .balanceAfter(savedSource.getBalance())
                .description(request.getDescription())
                .toAccountNumber(destAccount.getAccountNumber())
                .account(savedSource)
                .build();

        Transaction savedTxn = transactionRepository.save(transaction);

        // Audit Log
        auditLogService.log(
                email,
                "TRANSFER",
                "Account",
                savedSource.getId().toString(),
                "Transferred " + request.getAmount() + " from " + savedSource.getAccountNumber() + " to " + destAccount.getAccountNumber()
        );

        return TransactionResponse.from(savedTxn);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(String accountNumber, String email) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        // Verify ownership
        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorisedAccessException("You are not authorized to view the history of this account");
        }

        List<Transaction> transactions = transactionRepository.findByAccountOrderByCreatedAtDesc(account);
        return transactions.stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }
}
