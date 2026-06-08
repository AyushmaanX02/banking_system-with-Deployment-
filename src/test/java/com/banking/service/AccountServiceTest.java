package com.banking.service;

import com.banking.dto.request.AccountRequest;
import com.banking.dto.response.AccountResponse;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import com.banking.enums.AccountType;
import com.banking.enums.Role;
import com.banking.exception.AccountNotFoundException;
import com.banking.exception.UnauthorisedAccessException;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("Alice")
                .email("alice@example.com")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        account = Account.builder()
                .id(1L)
                .accountNumber("ACC111")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("createAccount: valid user → account saved, audit logged")
    void createAccount_success() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.createAccount("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo("ACC111");
        assertThat(response.getAccountType()).isEqualTo(AccountType.SAVINGS);
        verify(accountRepository).save(any(Account.class));
        verify(auditLogService).log(eq("alice@example.com"), eq("CREATE_ACCOUNT"), any(), any(), any());
    }

    @Test
    @DisplayName("createAccount: unknown email → throws UsernameNotFoundException")
    void createAccount_userNotFound_throws() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CURRENT);

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.createAccount("nobody@example.com", request))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMyAccounts: user has 2 accounts → list of 2 returned")
    void getMyAccounts_returnsAll() {
        Account account2 = Account.builder()
                .id(2L)
                .accountNumber("ACC222")
                .accountType(AccountType.CURRENT)
                .balance(BigDecimal.TEN)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(List.of(account, account2));

        List<AccountResponse> result = accountService.getMyAccounts("alice@example.com");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AccountResponse::getAccountNumber)
                .containsExactlyInAnyOrder("ACC111", "ACC222");
    }

    @Test
    @DisplayName("getAccount: owner requests own account → AccountResponse returned")
    void getAccount_ownerAccess_success() {
        when(accountRepository.findByAccountNumber("ACC111")).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount("ACC111", "alice@example.com");

        assertThat(response.getAccountNumber()).isEqualTo("ACC111");
        assertThat(response.getBalance()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("getAccount: non-owner request → throws UnauthorisedAccessException")
    void getAccount_nonOwner_throws() {
        when(accountRepository.findByAccountNumber("ACC111")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.getAccount("ACC111", "hacker@example.com"))
                .isInstanceOf(UnauthorisedAccessException.class);
    }

    @Test
    @DisplayName("getAccount: account does not exist → throws AccountNotFoundException")
    void getAccount_notFound_throws() {
        when(accountRepository.findByAccountNumber("BADACC")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("BADACC", "alice@example.com"))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
