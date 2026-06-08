package com.banking.dto.request;

import com.banking.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;
}
