package com.example.transaction.repository;

import com.example.transaction.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findTopByCurrencyOrderByDateDesc(String currency);
}
