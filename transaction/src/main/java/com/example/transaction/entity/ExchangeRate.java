package com.example.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "rate", nullable = false, scale = 4)
    private BigDecimal rate;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    //Сеттеры
    public void setRate(BigDecimal rate) {
        this.rate = rate.setScale(4, RoundingMode.HALF_UP);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    //Геттеры
    public BigDecimal getRate() {
        return rate.setScale(4, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getDate() {
        return date;
    }

    //Конструкторы
    public ExchangeRate() {
    }
}
