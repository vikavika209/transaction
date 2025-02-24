package com.example.transaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransactionLimitDTO {
    private String accountFrom;
    private String accountTo;
    private String currencyShortname;
    private BigDecimal sum;
    private String expenseCategory;
    private OffsetDateTime datetime;
    private BigDecimal limitSum;
    private OffsetDateTime limitDatetime;
    private String limitCurrencyShortname;

    public TransactionLimitDTO(String accountFrom, String accountTo, String currencyShortname,
                               BigDecimal sum, String expenseCategory, OffsetDateTime datetime,
                               BigDecimal limitSum, OffsetDateTime limitDatetime, String limitCurrencyShortname) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.currencyShortname = currencyShortname;
        this.sum = sum;
        this.expenseCategory = expenseCategory;
        this.datetime = datetime;
        this.limitSum = limitSum;
        this.limitDatetime = limitDatetime;
        this.limitCurrencyShortname = limitCurrencyShortname;
    }
}
