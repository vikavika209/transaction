package com.example.transaction.service;

import com.example.transaction.component.ExchangeRateApiClient;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateApiClient exchangeRateApiClient;

    @Autowired
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, ExchangeRateApiClient exchangeRateApiClient) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateApiClient = exchangeRateApiClient;
    }

    @Transactional
    public void updateExchangeRates() {
        saveExchangeRate("KZT");
        saveExchangeRate("RUB");
    }

    private void saveExchangeRate(String currency) {
        LocalDate today = LocalDate.now();
        BigDecimal rate = exchangeRateApiClient.getExchangeRate(currency);
        log.info("API вернул курс для {}: {}", currency, rate);

        if (rate == null) {
            ExchangeRate lastRate = getExchangeRate(currency);
            if (lastRate != null) {
                rate = lastRate.getRate();
            } else {
                log.error("Нет данных о предыдущем курсе для " + currency + ".");
                throw new RuntimeException("Нет данных о предыдущем курсе для " + currency);
            }
        }

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency(currency);
        exchangeRate.setDate(today);
        exchangeRate.setRate(rate);

        log.info("Курс для {} успешно обновлён {}", currency, today);
        exchangeRateRepository.save(exchangeRate);
    }

    public ExchangeRate getExchangeRate(String currency) {
        return exchangeRateRepository.findTopByCurrencyOrderByDateDesc(currency)
                .orElseGet(() -> {
                    log.warn("Не удалось получить курс валют для {}", currency);
                    return null;
                });
    }

    public BigDecimal convertCurrentCurrencyInUsd (BigDecimal sum, String currency) {
        BigDecimal rate = exchangeRateApiClient.getExchangeRate(currency);

        if (rate == null) {
            log.error("Не удалось получить курс для {}.", currency);
            throw new NoSuchElementException("Не удалось получить курс для валюты " + currency);
        }

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Некорректный курс валюты {}: {}", currency, rate);
            throw new IllegalArgumentException("Некорректный курс валюты " + currency);
        }

        return sum.divide(rate, 2, RoundingMode.HALF_UP);
    }
}

