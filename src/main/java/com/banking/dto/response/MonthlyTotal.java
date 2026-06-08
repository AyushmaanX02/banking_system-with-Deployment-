package com.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyTotal {
    private String month;          // e.g. "Jan 2025"
    private BigDecimal deposited;
    private BigDecimal withdrawn;
    private BigDecimal transferred;
}
