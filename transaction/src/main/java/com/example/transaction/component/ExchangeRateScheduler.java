package com.example.transaction.component;

import com.example.transaction.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateScheduler {

    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeRateScheduler(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public void updateRatesDaily() {
        exchangeRateService.updateExchangeRates();
    }
}
