package com.example.transaction.dto;

import com.example.transaction.entity.Limit;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class LimitDTO {
    @JsonProperty("account")
    private String account;

    @JsonProperty("limitCategory")
    private String limitCategory;

    @JsonProperty("limitSum")
    private BigDecimal limitSum;

    @JsonProperty("limitCurrencyShortName")
    private String limitCurrencyShortName;

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
