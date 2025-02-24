package com.example.transaction.dto;

import com.example.transaction.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class TransactionDTO {
    private String accountFrom;
    private String accountTo;
    private String currencyShortname;
    private BigDecimal sum;
    private String expenseCategory;

    public Transaction convertToTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAccountFrom(accountFrom);
        transaction.setAccountTo(accountTo);
        transaction.setCurrencyShortname(currencyShortname);
        transaction.setSum(sum);
        transaction.setExpenseCategory(expenseCategory);
        return transaction;
    }
    //Геттеры

    public String getAccountFrom() {
        return accountFrom;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public String getCurrencyShortname() {
        return currencyShortname;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    //Сеттеры

    public void setAccountFrom(String accountFrom) {
        this.accountFrom = accountFrom;
    }

    public void setAccountTo(String accountTo) {
        this.accountTo = accountTo;
    }

    public void setCurrencyShortname(String currencyShortname) {
        this.currencyShortname = currencyShortname;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

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
