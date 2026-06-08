package com.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalAccounts;
    private long activeAccounts;
    private long frozenAccounts;
    private long totalTransactions;
}
