package com.banking.dto.response;

import com.banking.entity.Beneficiary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {
    private Long id;
    private String nickname;
    private String accountNumber;
    private String bankName;

    public static BeneficiaryResponse from(Beneficiary b) {
        if (b == null) return null;
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .nickname(b.getNickname())
                .accountNumber(b.getAccountNumber())
                .bankName(b.getBankName())
                .build();
    }
}
