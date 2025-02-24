package com.example.transaction.service;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.entity.Limit;
import com.example.transaction.repository.LimitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class LimitService {

    private static final Logger logger = LoggerFactory.getLogger(LimitService.class);
    LimitRepository limitRepository;
    ExchangeRateService exchangeRateService;

    @Autowired
    public LimitService(LimitRepository limitRepository, ExchangeRateService exchangeRateService) {
        this.limitRepository = limitRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public Limit createLimit(LimitDTO limitDTO) {

        Limit limit = limitDTO.convertToLimit();

        Optional<Limit> optionalLimit = limitRepository.findTopByAccountAndLimitCategory(limitDTO.getAccount(), limitDTO.getLimitCategory());
        if (optionalLimit.isPresent()) {
            limit.setLimitDatetime(OffsetDateTime.now());
            limit.setLimitSum(limit.getLimitSum());
            return limitRepository.save(limit);
        }

        if (limit.getAccount() == null) {
            logger.error("Не указан аккаунт для установки лимита");
            throw new IllegalArgumentException("Не указан аккаунт для установки лимита");
        }

        if (limit.getLimitSum() == null) {
            limit.setLimitCurrencyShortName("USD");
            limit.setLimitSum(new BigDecimal("1000.00"));
        }

        if (!limit.getLimitCurrencyShortName().equalsIgnoreCase("USD")){

            BigDecimal limitSum = limit.getLimitSum();
            String limitCurrency = limit.getLimitCurrencyShortName();
            BigDecimal limitSumInUsd = exchangeRateService.convertCurrentCurrencyInUsd(limitSum, limitCurrency);

            limit.setLimitSum(limitSumInUsd);
            limit.setLimitCurrencyShortName("USD");
        }

        logger.info("Лимит для аккаунта: {} на категорию '{}' успешно установлен", limit.getLimitCategory(), limit.getAccount());
        return limitRepository.save(limit);
    }

    public Map<String, Limit> getLimitsByAccountNumber(String accountNumber) {

        Map<String, Limit> limitMap = new HashMap<>();

        limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(accountNumber, "service")
                .ifPresent(limit -> limitMap.put("service", limit));

        limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(accountNumber, "product")
                .ifPresent(limit -> limitMap.put("product", limit));

        logger.info("Лимиты для аккаунта {} получены.", accountNumber);
        return limitMap;
    }
}
