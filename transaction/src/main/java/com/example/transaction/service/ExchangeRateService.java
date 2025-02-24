package com.example.transaction.service;

import com.example.transaction.component.ExchangeRateApiClient;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateRepository.class);
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

        if (rate == null) {
            Optional<ExchangeRate> lastRate = exchangeRateRepository.findTopByCurrencyOrderByDateDesc(currency);
            if (lastRate.isPresent()) {
                rate = lastRate.get().getRate();
            } else {
                logger.error("Нет данных о предыдущем курсе для " + currency + ".");
                throw new RuntimeException("Нет данных о предыдущем курсе для " + currency);
            }
        }

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency(currency);
        exchangeRate.setDate(today);
        exchangeRate.setRate(rate);

        logger.info("Курс для {} успешно обновлён {}", currency, today);
        exchangeRateRepository.save(exchangeRate);
    }

    public ExchangeRate getExchangeRate(String currency) {
        return exchangeRateRepository.findTopByCurrencyOrderByDateDesc(currency)
                .orElseGet(() -> {
                    logger.warn("Не удалось получить курс валют для {}", currency);
                    return null;
                });
    }

    public BigDecimal convertCurrentCurrencyInUsd (BigDecimal sum, String currency) {
        BigDecimal rate = exchangeRateApiClient.getExchangeRate(currency);

        if (rate == null) {
            logger.error("Не удалось получить курс для {}.", currency);
            throw new NoSuchElementException("Не удалось получить курс для валюты " + currency);
        }

        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Некорректный курс валюты {}: {}", currency, rate);
            throw new IllegalArgumentException("Некорректный курс валюты " + currency);
        }

        return sum.divide(rate, 2, RoundingMode.HALF_UP);
    }
}

