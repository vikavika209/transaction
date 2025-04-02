package com.example.transaction.service;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.entity.Limit;
import com.example.transaction.repository.LimitRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Slf4j
public class LimitService {

    private final ModelMapper modelMapper;
    LimitRepository limitRepository;
    ExchangeRateService exchangeRateService;

    @Autowired
    public LimitService(ModelMapper modelMapper, LimitRepository limitRepository, ExchangeRateService exchangeRateService) {
        this.modelMapper = modelMapper;
        this.limitRepository = limitRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public Limit createLimit(LimitDTO limitDTO) {

        log.info("Метод createLimit начал работу");

        //Получение лимитов из репозитория
        Optional <Limit> optionalLimit = limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(limitDTO.getAccount(), limitDTO.getLimitCategory());
        Limit limit = optionalLimit.orElse(modelMapper.map(limitDTO, Limit.class));
        limit.setLimitDatetime(OffsetDateTime.now());

        //Получение или установка лимита
        getOrCreatLimitData(limit);

        log.info("Лимит для аккаунта: {} на категорию '{}' успешно установлен. Сумма лимита: {} {}.", limit.getAccount(), limit.getLimitCategory(), limit.getLimitSum(), limit.getLimitCurrencyShortName());
        return limitRepository.save(limit);
    }

    public Limit getLimitsByAccountNumberAndCategory(String accountNumber, String category) {

        Optional<Limit> optionalLimit = limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc(accountNumber, category);
        Limit limit = optionalLimit.orElse(null);
        log.info("Лимит в категории:{} для аккаунта {} получен.", category, accountNumber);
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

        log.info("Лимиты для аккаунта {} получены.", accountNumber);
        return limitMap;
    }

    private void getOrCreatLimitData(Limit limit) {

        //Установка лимита по умолчанию
        if (limit.getLimitCurrencyShortName() == null){
            log.info("Валюта не передана.");
            limit.setLimitSum(new BigDecimal("1000.00"));
            limit.setLimitCurrencyShortName("USD");

        }else {
            //Конвертация суммы лимита в USD
            if (limit.getLimitCurrencyShortName().equalsIgnoreCase("RUB") || limit.getLimitCurrencyShortName().equalsIgnoreCase("KZT")) {
                log.info("Валюта передана: {}.", limit.getLimitCurrencyShortName());

                BigDecimal limitSum = limit.getLimitSum();
                String limitCurrency = limit.getLimitCurrencyShortName();
                BigDecimal limitSumInUsd = exchangeRateService.convertCurrentCurrencyInUsd(limitSum, limitCurrency);
                log.info("Сумма лимита в {}: {}.", limitCurrency, limitSum);
                log.info("Сумма лимита в USD: {}.", limitSumInUsd);

                limit.setLimitSum(limitSumInUsd);
                limit.setLimitCurrencyShortName("USD");
            }

            //Установка лимита без конвертации
            else if (limit.getLimitCurrencyShortName().equalsIgnoreCase("USD")) {
                limit.setLimitSum(limit.getLimitSum());

            } else {
                log.error("Неизвестная валюта: {}", limit.getLimitCurrencyShortName());
                throw new RuntimeException("Неизвестная валюта");
            }
        }
    }

    public void deleteAllLimits(){
        limitRepository.deleteAll();
    }
}
