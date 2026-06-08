package com.banking.service;

import com.banking.dto.response.AnalyticsSummaryResponse;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import com.banking.enums.Role;
import com.banking.enums.TransactionType;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
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
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AnalyticsService analyticsService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("alice@example.com")
                .role(Role.CUSTOMER).enabled(true).build();

        account = Account.builder()
                .id(1L).accountNumber("ACC001")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("8000.00"))
                .status(AccountStatus.ACTIVE)
                .user(user).build();
    }

    private Transaction makeTransaction(TransactionType type, String amount) {
        return Transaction.builder()
                .id((long)(Math.random()*1000))
                .referenceNumber("REF" + System.nanoTime())
                .transactionType(type)
                .amount(new BigDecimal(amount))
                .balanceAfter(new BigDecimal("5000.00"))
                .account(account)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getSummaryForUser: returns non-null response with totals")
    void getSummaryForUser_returnsValidResponse() {
        List<Transaction> allTxns = List.of(
                makeTransaction(TransactionType.DEPOSIT, "5000.00"),
                makeTransaction(TransactionType.WITHDRAWAL, "1000.00"),
                makeTransaction(TransactionType.TRANSFER, "2000.00")
        );

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findAllByUserEmailOrderByCreatedAtDesc("alice@example.com"))
                .thenReturn(allTxns);
        when(transactionRepository.findByUserEmailAndDateRange(eq("alice@example.com"), any(), any()))
                .thenReturn(allTxns);
        when(accountRepository.findByUser(user)).thenReturn(List.of(account));

        AnalyticsSummaryResponse response = analyticsService.getSummaryForUser("alice@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getTotalDeposited()).isEqualByComparingTo("5000.00");
        assertThat(response.getTotalWithdrawn()).isEqualByComparingTo("1000.00");
        assertThat(response.getTotalTransferred()).isEqualByComparingTo("2000.00");
        assertThat(response.getTransactionCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("getSummaryForUser: no transactions → all totals are zero")
    void getSummaryForUser_noTransactions_zeroTotals() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findAllByUserEmailOrderByCreatedAtDesc("alice@example.com"))
                .thenReturn(List.of());
        when(transactionRepository.findByUserEmailAndDateRange(eq("alice@example.com"), any(), any()))
                .thenReturn(List.of());
        when(accountRepository.findByUser(user)).thenReturn(List.of(account));

        AnalyticsSummaryResponse response = analyticsService.getSummaryForUser("alice@example.com");

        assertThat(response.getTotalDeposited()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalWithdrawn()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTransactionCount()).isZero();
        assertThat(response.getMonthlyTotals()).hasSize(6); // always 6 months
    }

    @Test
    @DisplayName("getSummaryForUser: monthly totals has exactly 6 entries")
    void getSummaryForUser_monthlyTotals_hasSixMonths() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findAllByUserEmailOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(transactionRepository.findByUserEmailAndDateRange(any(), any(), any())).thenReturn(List.of());
        when(accountRepository.findByUser(user)).thenReturn(List.of(account));

        AnalyticsSummaryResponse response = analyticsService.getSummaryForUser("alice@example.com");

        assertThat(response.getMonthlyTotals()).hasSize(6);
    }

    @Test
    @DisplayName("getSummaryForUser: balance history has exactly 6 entries")
    void getSummaryForUser_balanceHistory_hasSixMonths() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.findAllByUserEmailOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(transactionRepository.findByUserEmailAndDateRange(any(), any(), any())).thenReturn(List.of());
        when(accountRepository.findByUser(user)).thenReturn(List.of(account));

        AnalyticsSummaryResponse response = analyticsService.getSummaryForUser("alice@example.com");

        assertThat(response.getBalanceHistory()).hasSize(6);
    }
}
