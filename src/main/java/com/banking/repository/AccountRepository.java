package com.banking.repository;

import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUser(User user);
    long countByStatus(AccountStatus status);
}
