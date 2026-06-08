package com.banking.dto.response;

import com.banking.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private List<AccountResponse> accounts;

    public static CustomerResponse from(User u) {
        if (u == null) return null;
        return CustomerResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .enabled(u.isEnabled())
                .createdAt(u.getCreatedAt())
                .accounts(u.getAccounts() != null ? 
                        u.getAccounts().stream().map(AccountResponse::from).collect(Collectors.toList()) : 
                        null)
                .build();
    }
}
