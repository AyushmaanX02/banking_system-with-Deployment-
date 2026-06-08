package com.banking.repository;

import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);

    List<Transaction> findAllByOrderByCreatedAtDesc();

    /** All transactions for a given user, ordered newest-first */
    @Query("SELECT t FROM Transaction t WHERE t.account.user.email = :email ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    /** Transactions for a given user within a date range */
    @Query("SELECT t FROM Transaction t WHERE t.account.user.email = :email " +
           "AND t.createdAt >= :from AND t.createdAt <= :to ORDER BY t.createdAt ASC")
    List<Transaction> findByUserEmailAndDateRange(
            @Param("email") String email,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** Sum of amount by transactionType for a given user */
    @Query("SELECT t.transactionType, SUM(t.amount) FROM Transaction t " +
           "WHERE t.account.user.email = :email " +
           "AND t.createdAt >= :from AND t.createdAt <= :to " +
           "GROUP BY t.transactionType")
    List<Object[]> sumByTypeForUser(
            @Param("email") String email,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
