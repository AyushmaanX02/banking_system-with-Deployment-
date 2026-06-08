package com.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryResponse {

    /** Last 6 months of transaction totals, oldest first */
    private List<MonthlyTotal> monthlyTotals;

    /** All-time totals */
    private BigDecimal totalDeposited;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalTransferred;

    /** Date-label → balance snapshot (one per month, last 6 months) */
    private Map<String, BigDecimal> balanceHistory;

    /** Number of transactions processed */
    private long transactionCount;
}
