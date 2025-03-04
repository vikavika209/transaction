package com.example.transaction.dto;

import com.example.transaction.entity.Limit;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class LimitDTO {
    @JsonProperty("account")
    private String account;

    @JsonProperty("limitCategory")
    private String limitCategory;

    @JsonProperty("limitSum")
    private BigDecimal limitSum;

    @JsonProperty("limitCurrencyShortName")
    private String limitCurrencyShortName;

    public Limit convertToLimit() {
        Limit limit = new Limit();
        limit.setAccount(account);
        limit.setLimitCategory(limitCategory);
        limit.setLimitSum(limitSum);
        limit.setLimitCurrencyShortName(limitCurrencyShortName);
        limit.setLimitDatetime(OffsetDateTime.now());
        return limit;
    }

    //Геттеры

    public String getAccount() {
        return account;
    }

    public String getLimitCategory() {
        return limitCategory;
    }

    public BigDecimal getLimitSum() {
        return limitSum;
    }

    public String getLimitCurrencyShortName() {
        return limitCurrencyShortName;
    }

    //Сеттеры

    public void setAccount(String account) {
        this.account = account;
    }

    public void setLimitCategory(String limitCategory) {
        this.limitCategory = limitCategory;
    }

    public void setLimitSum(BigDecimal limitSum) {
        this.limitSum = limitSum;
    }

    public void setLimitCurrencyShortName(String limitCurrencyShortName) {
        this.limitCurrencyShortName = limitCurrencyShortName;
    }

    //Конструктор

    public LimitDTO() {
    }
    public LimitDTO(String account, String limitCategory, BigDecimal limitSum, String limitCurrencyShortName) {
        this.account = account;
        this.limitCategory = limitCategory;
        this.limitSum = limitSum;
        this.limitCurrencyShortName = limitCurrencyShortName;
    }

    public LimitDTO(Limit limit) {
        this.account = limit.getAccount();
        this.limitCategory = limit.getLimitCategory();
        this.limitSum = limit.getLimitSum();
        this.limitCurrencyShortName = limit.getLimitCurrencyShortName();
    }

    public LimitDTO(String account, String limitCategory) {
        this.account = account;
        this.limitCategory = limitCategory;
    }
}
