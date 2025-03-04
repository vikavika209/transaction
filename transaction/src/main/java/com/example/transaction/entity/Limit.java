package com.example.transaction.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "limits")
public class Limit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account", nullable = false, length = 10)
    @Pattern(regexp = "\\d{10}", message = "Аккаунт должен состоять из 10 цифр")
    private String account; //Использую String, т.к. аккаунт может начинаться с "0".

    @Column(name = "limit_category", nullable = false)
    private String limitCategory;

    @Column(name = "limit_sum", nullable = false, precision = 19, scale = 2)
    private BigDecimal limitSum;

    @Column(name = "limit_datetime", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime limitDatetime;

    @Column(name = "limit_currency_shortname", nullable = false)
    private String limitCurrencyShortName;

    @OneToMany(mappedBy = "limit", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    List<Transaction> transactions;

    //Сеттеры
    public void setLimitSum (BigDecimal limitSum) {
            this.limitSum = limitSum.setScale(2, RoundingMode.HALF_UP);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAccount (String account) {
        this.account = account;
    }

    public void setLimitCategory(String limitCategory) {
        this.limitCategory = limitCategory;
    }

    public void setLimitCurrencyShortName(String limitCurrencyShortname) {
        this.limitCurrencyShortName = limitCurrencyShortname;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setLimitDatetime(OffsetDateTime limitDatetime) {
        this.limitDatetime = limitDatetime;
    }

//Геттеры

    public Integer getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public String getLimitCategory() {
        return limitCategory;
    }

    public BigDecimal getLimitSum() {
        return limitSum;
    }

    public OffsetDateTime getLimitDatetime() {
        return limitDatetime;
    }

    public String getLimitCurrencyShortName() {
        return limitCurrencyShortName;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    //Конструкторы

    public Limit() {
    }

    public Limit(String account, String limitCategory, BigDecimal limitSum, String limitCurrencyShortName) {
        this.account = account;
        this.limitCategory = limitCategory;
        this.limitSum = limitSum;
        this.limitCurrencyShortName = limitCurrencyShortName;
    }
}
