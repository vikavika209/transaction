package com.example.transaction.service;

import com.example.transaction.component.ExchangeRateApiClient;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    Logger logger = LoggerFactory.getLogger(ExchangeRateServiceTest.class);

    ExchangeRate exchangeRateForKzt;
    ExchangeRate exchangeRateForRub;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateForKzt = ExchangeRate.builder()
                .date(LocalDate.now())
                .rate(new BigDecimal("100"))
                .currency("KZT")
                .build();
        exchangeRateForRub = ExchangeRate.builder()
                .date(LocalDate.now())
                .rate(new BigDecimal("200"))
                .currency("RUB")
                .build();
    }

    @Test
    void testUpdateExchangeRates_Success() {

        when(exchangeRateApiClient.getExchangeRate("KZT")).thenReturn(exchangeRateForKzt.getRate());
        when(exchangeRateApiClient.getExchangeRate("RUB")).thenReturn(exchangeRateForRub.getRate());

        logger.info("Курс для KZT: {}", exchangeRateApiClient.getExchangeRate("KZT"));
        logger.info("Курс для RUB: {}", exchangeRateApiClient.getExchangeRate("RUB"));

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