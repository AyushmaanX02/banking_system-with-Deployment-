package com.banking.dto.response;

import com.banking.entity.Transaction;
import com.banking.enums.TransactionType;
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
public class TransactionResponse {
    private Long id;
    private String referenceNumber;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String toAccountNumber;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        if (t == null) return null;
        return TransactionResponse.builder()
                .id(t.getId())
                .referenceNumber(t.getReferenceNumber())
                .transactionType(t.getTransactionType())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .description(t.getDescription())
                .toAccountNumber(t.getToAccountNumber())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
