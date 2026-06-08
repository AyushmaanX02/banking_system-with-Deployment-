package com.banking.dto.response;

import com.banking.entity.Account;
import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public static AccountResponse from(Account a) {
        if (a == null) return null;
        return AccountResponse.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .accountType(a.getAccountType())
                .balance(a.getBalance())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
