package com.example.transaction.service;

import com.example.transaction.component.ExchangeRateApiClient;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @Test
    void testUpdateExchangeRates_Success() {
        when(exchangeRateApiClient.getExchangeRate("KZT")).thenReturn(BigDecimal.valueOf(500));
        when(exchangeRateApiClient.getExchangeRate("RUB")).thenReturn(BigDecimal.valueOf(90));

        exchangeRateService.updateExchangeRates();

        verify(exchangeRateRepository, times(2)).save(any(ExchangeRate.class));
    }

    @Test
    void testUpdateExchangeRates_NoApiData_UsesLastRate() {
        ExchangeRate lastRateKZT = new ExchangeRate();
        lastRateKZT.setCurrency("KZT");
        lastRateKZT.setRate(BigDecimal.valueOf(495));

        ExchangeRate lastRateRUB = new ExchangeRate();
        lastRateRUB.setCurrency("RUB");
        lastRateRUB.setRate(BigDecimal.valueOf(91));

        when(exchangeRateApiClient.getExchangeRate("KZT")).thenReturn(null);
        when(exchangeRateRepository.findTopByCurrencyOrderByDateDesc("KZT")).thenReturn(Optional.of(lastRateKZT));

        when(exchangeRateApiClient.getExchangeRate("RUB")).thenReturn(null);
        when(exchangeRateRepository.findTopByCurrencyOrderByDateDesc("RUB")).thenReturn(Optional.of(lastRateRUB));

        exchangeRateService.updateExchangeRates();

        verify(exchangeRateRepository, times(2)).save(any(ExchangeRate.class));
    }

    @Test
    void testUpdateExchangeRates_NoApiData_NoLastRate_ThrowsException() {
        when(exchangeRateApiClient.getExchangeRate("KZT")).thenReturn(null);
        when(exchangeRateRepository.findTopByCurrencyOrderByDateDesc("KZT")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> exchangeRateService.updateExchangeRates());
    }
}