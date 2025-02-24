package com.example.transaction.repository;

import com.example.transaction.dto.TransactionLimitDTO;
import com.example.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("""
    SELECT new com.example.transaction.dto.TransactionLimitDTO(
        t.accountFrom, t.accountTo, t.currencyShortname, t.sum,
        t.expenseCategory, t.transactionTime, l.limitSum,
        l.limitDatetime, l.limitCurrencyShortName
    )
    FROM Transaction t
    LEFT JOIN Limit l
        ON t.accountFrom = l.account 
        AND t.expenseCategory = l.limitCategory
    WHERE t.limitExceeded = true 
    AND t.accountFrom = :accountFrom
""")
    List<TransactionLimitDTO> findExceededTransactions(@Param("accountFrom") String accountFrom);
}
