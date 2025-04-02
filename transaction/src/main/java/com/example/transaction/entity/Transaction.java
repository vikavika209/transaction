package com.example.transaction.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
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
    @JoinColumn(name = "limit_id", nullable = false)
    @JsonBackReference
    private Limit limit;

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
