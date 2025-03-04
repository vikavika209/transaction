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

        logger.info("Метод createLimit начал работу");

        Optional <Limit> optionalLimit = limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(limitDTO.getAccount(), limitDTO.getLimitCategory());
        Limit limit = optionalLimit.orElse(new Limit());

        limit.setAccount(limitDTO.getAccount());
        limit.setLimitCategory(limitDTO.getLimitCategory());
        limit.setLimitDatetime(OffsetDateTime.now());

        if (limitDTO.getLimitCurrencyShortName() == null){
            limit.setLimitSum(new BigDecimal("1000.00"));
            limit.setLimitCurrencyShortName("USD");
        }else {
            if (limitDTO.getLimitCurrencyShortName().equalsIgnoreCase("RUB") || limitDTO.getLimitCurrencyShortName().equalsIgnoreCase("KZT")) {

                BigDecimal limitSum = limitDTO.getLimitSum();
                String limitCurrency = limitDTO.getLimitCurrencyShortName();
                BigDecimal limitSumInUsd = exchangeRateService.convertCurrentCurrencyInUsd(limitSum, limitCurrency);

                limit.setLimitSum(limitSumInUsd);
                limit.setLimitCurrencyShortName("USD");
            }

            else if (limitDTO.getLimitCurrencyShortName().equalsIgnoreCase("USD")) {
                limit.setLimitSum(limitDTO.getLimitSum());
            } else {
                logger.error("Неизвестная валюта: {}", limitDTO.getLimitCurrencyShortName());
                throw new RuntimeException("Неизвестная валюта");
            }
        }

        logger.info("Лимит для аккаунта: {} на категорию '{}' успешно установлен", limit.getLimitCategory(), limit.getAccount());
        return limitRepository.save(limit);
    }

    public Limit getLimitsByAccountNumberAndCategory(String accountNumber, String category) {

        Optional<Limit> optionalLimit = limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(accountNumber, category);
        Limit limit = optionalLimit.orElse(null);
        logger.info("Лимит в категории:{} для аккаунта {} получен.", category, accountNumber);
        return limit;
    }

    public Map<String, Limit> getLimitsByAccountNumber(String accountNumber){
        Limit productLimit = getLimitsByAccountNumberAndCategory(accountNumber, "product");
        Limit serviceLimit = getLimitsByAccountNumberAndCategory(accountNumber, "service");

        Map<String, Limit> limitMap = new HashMap<>();

        if (productLimit == null) {
            productLimit = createLimit(new LimitDTO(accountNumber, "product"));
        }

        if (serviceLimit == null) {
            serviceLimit = createLimit(new LimitDTO(accountNumber, "service"));
        }

        limitMap.put("product", productLimit);
        limitMap.put("service", serviceLimit);

        logger.info("Лимиты для аккаунта {} получены.", accountNumber);
        return limitMap;
    }
}
