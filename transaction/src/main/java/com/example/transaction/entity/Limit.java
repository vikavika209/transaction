package com.example.transaction.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(
        name = "limits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account", "limit_category", "limit_datetime", "limit_sum"})
)
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
