package com.banking.service;

import com.banking.dto.response.AnalyticsSummaryResponse;
import com.banking.dto.response.MonthlyTotal;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.enums.TransactionType;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    public AnalyticsSummaryResponse getSummaryForUser(String email) {
        // Validate user exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixMonthsAgo = now.minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay();

        // ── Monthly breakdown (last 6 months) ──────────────────────────
        List<MonthlyTotal> monthlyTotals = buildMonthlyTotals(email, sixMonthsAgo, now);

        // ── All-time totals ────────────────────────────────────────────
        List<Transaction> allTransactions =
                transactionRepository.findAllByUserEmailOrderByCreatedAtDesc(email);

        BigDecimal totalDeposited = sumByType(allTransactions, TransactionType.DEPOSIT);
        BigDecimal totalWithdrawn = sumByType(allTransactions, TransactionType.WITHDRAWAL);
        BigDecimal totalTransferred = sumByType(allTransactions, TransactionType.TRANSFER);

        // ── Balance history (last 6 months, one snapshot per month) ────
        Map<String, BigDecimal> balanceHistory = buildBalanceHistory(email, sixMonthsAgo, now);

        return AnalyticsSummaryResponse.builder()
                .monthlyTotals(monthlyTotals)
                .totalDeposited(totalDeposited)
                .totalWithdrawn(totalWithdrawn)
                .totalTransferred(totalTransferred)
                .balanceHistory(balanceHistory)
                .transactionCount(allTransactions.size())
                .build();
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private List<MonthlyTotal> buildMonthlyTotals(String email,
                                                   LocalDateTime from,
                                                   LocalDateTime to) {
        List<Transaction> rangeTransactions =
                transactionRepository.findByUserEmailAndDateRange(email, from, to);

        // Group by "MMM yyyy"
        Map<String, List<Transaction>> byMonth = rangeTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCreatedAt().format(MONTH_FMT)));

        // Build an ordered list for the last 6 months (even if no data)
        List<MonthlyTotal> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            String label = to.minusMonths(i).format(MONTH_FMT);
            List<Transaction> monthTxns = byMonth.getOrDefault(label, List.of());
            result.add(MonthlyTotal.builder()
                    .month(label)
                    .deposited(sumByType(monthTxns, TransactionType.DEPOSIT))
                    .withdrawn(sumByType(monthTxns, TransactionType.WITHDRAWAL))
                    .transferred(sumByType(monthTxns, TransactionType.TRANSFER))
                    .build());
        }
        return result;
    }

    private Map<String, BigDecimal> buildBalanceHistory(String email,
                                                         LocalDateTime from,
                                                         LocalDateTime to) {
        // Get all accounts for the user and sum their current balances
        List<Account> accounts = accountRepository.findByUser(
                userRepository.findByEmail(email).orElseThrow());
        BigDecimal currentBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Walk backwards through monthly transactions to reconstruct past balances
        Map<String, BigDecimal> history = new LinkedHashMap<>();
        BigDecimal runningBalance = currentBalance;

        for (int i = 0; i < 6; i++) {
            LocalDateTime monthEnd = to.minusMonths(i);
            LocalDateTime monthStart = monthEnd.minusMonths(1);
            String label = monthEnd.format(MONTH_FMT);

            history.put(label, runningBalance.max(BigDecimal.ZERO));

            if (i < 5) {
                List<Transaction> txns = transactionRepository
                        .findByUserEmailAndDateRange(email, monthStart, monthEnd);
                for (Transaction t : txns) {
                    if (t.getTransactionType() == TransactionType.DEPOSIT) {
                        runningBalance = runningBalance.subtract(t.getAmount());
                    } else if (t.getTransactionType() == TransactionType.WITHDRAWAL ||
                               t.getTransactionType() == TransactionType.TRANSFER) {
                        runningBalance = runningBalance.add(t.getAmount());
                    }
                }
            }
        }

        // Reverse so oldest month is first
        LinkedHashMap<String, BigDecimal> ordered = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>(history.keySet());
        Collections.reverse(keys);
        keys.forEach(k -> ordered.put(k, history.get(k)));
        return ordered;
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getTransactionType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
