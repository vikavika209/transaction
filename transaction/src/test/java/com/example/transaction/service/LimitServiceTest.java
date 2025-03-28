package com.example.transaction.service;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.entity.Limit;
import com.example.transaction.repository.LimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(LimitServiceTest.class);

    BigDecimal limitSumInRub;
    BigDecimal limitSumInUsd;
    LimitDTO limitDTO;
    Limit limit;


    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    LimitRepository limitRepository;

    @Mock
    ExchangeRateService exchangeRateService;

    @InjectMocks
    LimitService limitService;

    @BeforeEach
    void setUp() {
        limitSumInRub = new BigDecimal(200.00);
        limitSumInUsd = new BigDecimal(2.00);
        limitDTO = new LimitDTO("0123456789", "product", limitSumInRub, "RUB");
        limit = new Limit("0123456789", "product", limitSumInUsd, "USD");
    }

    @Test
    void createLimit() {
        when(limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc("0123456789", "product"))
                .thenReturn(Optional.empty());
        when(exchangeRateService.convertCurrentCurrencyInUsd(limitSumInRub, "RUB")).thenReturn(limitSumInUsd);
        when(limitRepository.save(any(Limit.class))).thenReturn(limit);

        Limit createdLimit  = limitService.createLimit(limitDTO);

        assertEquals("0123456789", createdLimit.getAccount());
        assertEquals("product", createdLimit.getLimitCategory());

        verify(limitRepository, times(1)).save(any(Limit.class));
        verify(limitRepository, times(1)).findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc("0123456789", "product");

    }

    @Test
    void getLimitsByAccountNumberAndCategory() {
        when(limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc("0123456789", "product"))
                .thenReturn(Optional.of(limit));

        Limit limitsByAccountNumberAndCategory = limitService.getLimitsByAccountNumberAndCategory("0123456789", "product");

        assertEquals("0123456789", limitsByAccountNumberAndCategory.getAccount());
        assertEquals("product", limitsByAccountNumberAndCategory.getLimitCategory());
        assertEquals(limitSumInUsd, limitsByAccountNumberAndCategory.getLimitSum());
    }

    @Test
    void getLimitsByAccountNumber() {

        when(limitRepository.findTopByAccountAndLimitCategoryOrderByLimitDatetimeDesc("0123456789", "product"))
                .thenReturn(Optional.empty());

        Map<String, Limit> limitsByAccountNumber = limitService.getLimitsByAccountNumber("0123456789");

        assertNotNull(limitsByAccountNumber);
        assertEquals(2, limitsByAccountNumber.size());

    }
}