package com.banking.service;

import com.banking.dto.request.DepositRequest;
import com.banking.dto.request.TransferRequest;
import com.banking.dto.response.TransactionResponse;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import com.banking.enums.Role;
import com.banking.enums.TransactionType;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.InsufficientFundsException;
import com.banking.exception.UnauthorisedAccessException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private TransactionService transactionService;

    private User owner;
    private Account activeAccount;
    private Account frozenAccount;
    private Account destAccount;
    private Transaction savedTransaction;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("owner@example.com").role(Role.CUSTOMER).enabled(true).build();

        activeAccount = Account.builder()
                .id(1L).accountNumber("ACC001")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("10000.00"))
                .status(AccountStatus.ACTIVE)
                .user(owner).build();

        frozenAccount = Account.builder()
                .id(2L).accountNumber("ACC002")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .status(AccountStatus.FROZEN)
                .user(owner).build();

        User other = User.builder().id(2L).email("other@example.com").role(Role.CUSTOMER).enabled(true).build();
        destAccount = Account.builder()
                .id(3L).accountNumber("ACC003")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("2000.00"))
                .status(AccountStatus.ACTIVE)
                .user(other).build();

        savedTransaction = Transaction.builder()
                .id(1L).referenceNumber("DEP123")
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .balanceAfter(new BigDecimal("10500.00"))
                .account(activeAccount)
                .createdAt(LocalDateTime.now()).build();
    }

    // ─── DEPOSIT ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deposit: valid request → balance updated, transaction saved")
    void deposit_success() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC001");
        req.setAmount(new BigDecimal("500.00"));

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(activeAccount)).thenReturn(activeAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.deposit("owner@example.com", req);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(activeAccount.getBalance()).isEqualByComparingTo("10500.00");
        verify(transactionRepository).save(any(Transaction.class));
        verify(auditLogService).log(eq("owner@example.com"), eq("DEPOSIT"), any(), any(), any());
    }

    @Test
    @DisplayName("deposit: account not found → throws AccountNotFoundException")
    void deposit_accountNotFound_throws() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("BADACC");
        req.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumber("BADACC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deposit("owner@example.com", req))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("deposit: non-owner → throws UnauthorisedAccessException")
    void deposit_notOwner_throws() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC001");
        req.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> transactionService.deposit("hacker@example.com", req))
                .isInstanceOf(UnauthorisedAccessException.class);
    }

    @Test
    @DisplayName("deposit: frozen account → throws UnauthorisedAccessException")
    void deposit_frozenAccount_throws() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC002");
        req.setAmount(BigDecimal.TEN);

        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(frozenAccount));

        assertThatThrownBy(() -> transactionService.deposit("owner@example.com", req))
                .isInstanceOf(UnauthorisedAccessException.class)
                .hasMessageContaining("FROZEN");
    }

    // ─── WITHDRAW ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("withdraw: sufficient funds → balance reduced, transaction saved")
    void withdraw_success() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC001");
        req.setAmount(new BigDecimal("1000.00"));

        Transaction wthTxn = Transaction.builder()
                .id(2L).referenceNumber("WTH123")
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("1000.00"))
                .balanceAfter(new BigDecimal("9000.00"))
                .account(activeAccount)
                .createdAt(LocalDateTime.now()).build();

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.save(activeAccount)).thenReturn(activeAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(wthTxn);

        TransactionResponse response = transactionService.withdraw("owner@example.com", req);

        assertThat(response.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(activeAccount.getBalance()).isEqualByComparingTo("9000.00");
        verify(auditLogService).log(eq("owner@example.com"), eq("WITHDRAWAL"), any(), any(), any());
    }

    @Test
    @DisplayName("withdraw: insufficient funds → throws InsufficientFundsException")
    void withdraw_insufficientFunds_throws() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC001");
        req.setAmount(new BigDecimal("99999.00"));

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> transactionService.withdraw("owner@example.com", req))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    @DisplayName("withdraw: frozen account → throws UnauthorisedAccessException")
    void withdraw_frozenAccount_throws() {
        DepositRequest req = new DepositRequest();
        req.setAccountNumber("ACC002");
        req.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber("ACC002")).thenReturn(Optional.of(frozenAccount));

        assertThatThrownBy(() -> transactionService.withdraw("owner@example.com", req))
                .isInstanceOf(UnauthorisedAccessException.class)
                .hasMessageContaining("FROZEN");
    }

    // ─── TRANSFER ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("transfer: valid request → balances updated on both accounts")
    void transfer_success() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("ACC001");
        req.setToAccountNumber("ACC003");
        req.setAmount(new BigDecimal("2000.00"));

        Transaction trfTxn = Transaction.builder()
                .id(3L).referenceNumber("TRF123")
                .transactionType(TransactionType.TRANSFER)
                .amount(new BigDecimal("2000.00"))
                .balanceAfter(new BigDecimal("8000.00"))
                .toAccountNumber("ACC003")
                .account(activeAccount)
                .createdAt(LocalDateTime.now()).build();

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACC003")).thenReturn(Optional.of(destAccount));
        when(accountRepository.save(activeAccount)).thenReturn(activeAccount);
        when(accountRepository.save(destAccount)).thenReturn(destAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(trfTxn);

        TransactionResponse response = transactionService.transfer("owner@example.com", req);

        assertThat(response.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(activeAccount.getBalance()).isEqualByComparingTo("8000.00");
        assertThat(destAccount.getBalance()).isEqualByComparingTo("4000.00");
        verify(auditLogService).log(eq("owner@example.com"), eq("TRANSFER"), any(), any(), any());
    }

    @Test
    @DisplayName("transfer: same account → throws UnauthorisedAccessException")
    void transfer_sameAccount_throws() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("ACC001");
        req.setToAccountNumber("ACC001");
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> transactionService.transfer("owner@example.com", req))
                .isInstanceOf(UnauthorisedAccessException.class)
                .hasMessageContaining("same account");
    }

    @Test
    @DisplayName("transfer: insufficient funds → throws InsufficientFundsException")
    void transfer_insufficientFunds_throws() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("ACC001");
        req.setToAccountNumber("ACC003");
        req.setAmount(new BigDecimal("99999.00"));

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACC003")).thenReturn(Optional.of(destAccount));

        assertThatThrownBy(() -> transactionService.transfer("owner@example.com", req))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    @DisplayName("transfer: destination frozen → throws UnauthorisedAccessException")
    void transfer_destFrozen_throws() {
        // Create a second frozen account owned by someone else
        User other2 = User.builder().id(3L).email("other2@example.com").role(Role.CUSTOMER).enabled(true).build();
        Account frozenDest = Account.builder()
                .id(4L).accountNumber("ACC004")
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.FROZEN)
                .user(other2).build();

        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("ACC001");
        req.setToAccountNumber("ACC004");
        req.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACC004")).thenReturn(Optional.of(frozenDest));

        assertThatThrownBy(() -> transactionService.transfer("owner@example.com", req))
                .isInstanceOf(UnauthorisedAccessException.class)
                .hasMessageContaining("FROZEN");
    }

    @Test
    @DisplayName("transfer: non-owner of source → throws UnauthorisedAccessException")
    void transfer_notOwnerOfSource_throws() {
        TransferRequest req = new TransferRequest();
        req.setFromAccountNumber("ACC001");
        req.setToAccountNumber("ACC003");
        req.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(accountRepository.findByAccountNumber("ACC003")).thenReturn(Optional.of(destAccount));

        assertThatThrownBy(() -> transactionService.transfer("hacker@example.com", req))
                .isInstanceOf(UnauthorisedAccessException.class);
    }

    // ─── GET HISTORY ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getHistory: owner request → returns transaction list")
    void getHistory_ownerAccess_success() {
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.findByAccountOrderByCreatedAtDesc(activeAccount))
                .thenReturn(List.of(savedTransaction));

        List<TransactionResponse> result = transactionService.getHistory("ACC001", "owner@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReferenceNumber()).isEqualTo("DEP123");
    }

    @Test
    @DisplayName("getHistory: non-owner request → throws UnauthorisedAccessException")
    void getHistory_nonOwner_throws() {
        when(accountRepository.findByAccountNumber("ACC001")).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> transactionService.getHistory("ACC001", "hacker@example.com"))
                .isInstanceOf(UnauthorisedAccessException.class);
    }
}
