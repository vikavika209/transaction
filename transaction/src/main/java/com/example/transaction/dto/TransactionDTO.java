package com.example.transaction.dto;

import com.example.transaction.entity.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionDTO {
    private String accountFrom;
    private String accountTo;
    private String currencyShortname;
    private BigDecimal sum;
    private String expenseCategory;

    //Конструктор

    public TransactionDTO() {
    }

    public TransactionDTO(String accountFrom, String accountTo, String currencyShortname, BigDecimal sum, String expenseCategory) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.currencyShortname = currencyShortname;
        this.sum = sum;
        this.expenseCategory = expenseCategory;
    }

    public TransactionDTO(Transaction transaction) {
        this.accountFrom = transaction.getAccountFrom();
        this.accountTo = transaction.getAccountTo();
        this.currencyShortname = transaction.getCurrencyShortname();
        this.sum = transaction.getSum();
        this.expenseCategory = transaction.getExpenseCategory();
    }
}
