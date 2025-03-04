package com.example.transaction.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_from", nullable = false, length = 10, insertable = false, updatable = false)
    @Pattern(regexp = "\\d{10}", message = "Аккаунт должен состоять из 10 цифр")
    private String accountFrom; //Использую String, т.к. аккаунт может начинаться с "0".

    @Column(name = "account_to", nullable = false, length = 10)
    @Pattern(regexp = "\\d{10}", message = "Аккаунт должен состоять из 10 цифр")
    private String accountTo; //Использую String, т.к. аккаунт может начинаться с "0".

    @Column(name = "currency_shortname", nullable = false)
    private String currencyShortname;

    @Column(name = "sum", nullable = false, precision = 19, scale = 2)
    private BigDecimal sum;

    @Column(name = "expense_category", insertable = false, updatable = false)
    private String expenseCategory;

    @Column(name = "transaction_time", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime transactionTime;

    @Column(name = "limit_exceeded", nullable = false)
    private boolean limitExceeded;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "account_from", referencedColumnName = "account"),
            @JoinColumn(name = "expense_category", referencedColumnName = "limit_category")
    })
    @JsonBackReference
    private Limit limit;

    //Сеттеры
    public void setSum(BigDecimal sum) {
        this.sum = sum.setScale(2, RoundingMode.HALF_UP);
    }

    public void setAccountFrom(@Pattern(regexp = "\\d{10}", message = "Аккаунт должен состоять из 10 цифр") String accountFrom) {
        this.accountFrom = accountFrom;
    }

    public void setAccountTo(@Pattern(regexp = "\\d{10}", message = "Аккаунт должен состоять из 10 цифр") String accountTo) {
        this.accountTo = accountTo;
    }

    public void setCurrencyShortname(String currencyShortname) {
        this.currencyShortname = currencyShortname;
    }

    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    public void setTransactionTime(OffsetDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    //Геттеры

    public BigDecimal getSum() {
        return sum != null ? sum.setScale(2, RoundingMode.HALF_UP) : null;
    }

    public Integer getId() {
        return id;
    }

    public String getAccountFrom() {
        return accountFrom;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public String getCurrencyShortname() {
        return currencyShortname;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    public OffsetDateTime getTransactionTime() {
        return transactionTime;
    }

    public boolean isLimitExceeded() {
        return limitExceeded;
    }

    public Limit getLimit() {
        return limit;
    }

    //Конструкторы
    public Transaction() {
    }

    @PrePersist
    protected void onCreate() {
        if (transactionTime == null) {
            transactionTime = OffsetDateTime.now();
        }
    }
}
