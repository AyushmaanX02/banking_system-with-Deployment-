package com.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryRequest {

    private String nickname;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;
}
