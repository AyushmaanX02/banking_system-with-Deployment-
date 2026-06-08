package com.banking.service;

import com.banking.dto.request.AccountRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.UnauthorisedAccessException;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AccountResponse createAccount(String email, AccountRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        Account account = Account.builder()
                .accountNumber("ACC" + System.currentTimeMillis())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        Account savedAccount = accountRepository.save(account);

        auditLogService.log(
                email,
                "CREATE_ACCOUNT",
                "Account",
                savedAccount.getId().toString(),
                "Created account " + savedAccount.getAccountNumber() + " (" + savedAccount.getAccountType() + ")"
        );

        return AccountResponse.from(savedAccount);
    }

    public List<AccountResponse> getMyAccounts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        List<Account> accounts = accountRepository.findByUser(user);
        return accounts.stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccount(String accountNumber, String email) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        if (!account.getUser().getEmail().equals(email)) {
            throw new UnauthorisedAccessException("You are not authorized to access this account");
        }

        return AccountResponse.from(account);
    }
}
