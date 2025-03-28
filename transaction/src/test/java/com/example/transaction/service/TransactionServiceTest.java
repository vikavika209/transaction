package com.example.transaction.service;

import com.example.transaction.config.AppConfig;
import com.example.transaction.dto.LimitDTO;
import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.entity.Limit;
import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    private TransactionDTO transactionOverLimitDto;
    private TransactionDTO transactionUnderLimitDto;
    private Limit limit;
    private ExchangeRate exchangeRate;
    private final Map<String, Limit> map = new HashMap<>();

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private LimitService limitService;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionOverLimitDto = new TransactionDTO();
        transactionOverLimitDto.setAccountFrom("0123456789");
        transactionOverLimitDto.setCurrencyShortname("RUB");
        transactionOverLimitDto.setSum(new BigDecimal("10000.00"));
        transactionOverLimitDto.setExpenseCategory("service");

        transactionUnderLimitDto = new TransactionDTO();
        transactionUnderLimitDto.setAccountFrom("0123456789");
        transactionUnderLimitDto.setCurrencyShortname("RUB");
        transactionUnderLimitDto.setSum(new BigDecimal("2000.00"));
        transactionUnderLimitDto.setExpenseCategory("service");

        limit = new Limit();
        limit.setAccount("0123456789");
        limit.setLimitCategory("service");
        limit.setLimitSum(new BigDecimal("50.00"));
        limit.setLimitCurrencyShortName("USD");

        exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency("RUB");
        exchangeRate.setRate(BigDecimal.valueOf(100.00));
    }

    @Test
    void setLimitExceededTrueWhenLimitExceeded() {

        when(exchangeRateService.convertCurrentCurrencyInUsd(any(BigDecimal.class), any(String.class)))
                .thenAnswer(invocation -> {
                    BigDecimal sum = invocation.getArgument(0);
                    return sum.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                });
        when(limitService.getLimitsByAccountNumberAndCategory("0123456789", "service")).thenReturn(limit);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTransaction = transactionService.processTransaction(transactionOverLimitDto);
        Assertions.assertNotNull(savedTransaction);
        Assertions.assertTrue(savedTransaction.isLimitExceeded());
        Assertions.assertEquals("RUB", savedTransaction.getCurrencyShortname());
        Assertions.assertEquals(new BigDecimal("10000.00"), savedTransaction.getSum());
    }

    @Test
    void setLimitExceededFalseWhenLimitNotExceeded() {

        when(exchangeRateService.convertCurrentCurrencyInUsd(any(BigDecimal.class), any(String.class)))
                .thenAnswer(invocation -> {
                    BigDecimal sum = invocation.getArgument(0);
                    return sum.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                });
        when(limitService.getLimitsByAccountNumberAndCategory("0123456789", "service")).thenReturn(limit);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTransaction = transactionService.processTransaction(transactionUnderLimitDto);
        Assertions.assertNotNull(savedTransaction);
        Assertions.assertFalse(savedTransaction.isLimitExceeded());
    }

    @Test
    void setNewLimit(){
        when(exchangeRateService.convertCurrentCurrencyInUsd(any(BigDecimal.class), any(String.class)))
                .thenAnswer(invocation -> {
                    BigDecimal sum = invocation.getArgument(0);
                    return sum.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                });
        when(limitService.getLimitsByAccountNumberAndCategory("0123456789", "service")).thenReturn(limit);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transactionOverOldLimit = transactionService.processTransaction(transactionOverLimitDto);
        Assertions.assertTrue(transactionOverOldLimit.isLimitExceeded());

        Limit newLimit = new Limit("0123456789", "service", new BigDecimal("200.00"), "USD");
        when(limitService.createLimit(any(LimitDTO.class))).thenReturn(newLimit);

        limitService.createLimit(new LimitDTO("0123456789", "service", new BigDecimal("200.00"), "USD"));

        when(limitService.getLimitsByAccountNumberAndCategory("0123456789", "service")).thenReturn(newLimit);

        Transaction transactionUnderNewLimit = transactionService.processTransaction(transactionOverLimitDto);
        Assertions.assertTrue(transactionOverOldLimit.isLimitExceeded());
        Assertions.assertEquals("RUB", transactionOverOldLimit.getCurrencyShortname());
        Assertions.assertEquals(new BigDecimal("10000.00"), transactionOverOldLimit.getSum());

        Assertions.assertFalse(transactionUnderNewLimit.isLimitExceeded());
        Assertions.assertEquals("RUB", transactionUnderNewLimit.getCurrencyShortname());
        Assertions.assertEquals(new BigDecimal("10000.00"), transactionUnderNewLimit.getSum());

        TransactionDTO transactionOverNewLimitDTO = new TransactionDTO();
        transactionOverNewLimitDTO.setAccountFrom("0123456789");
        transactionOverNewLimitDTO.setCurrencyShortname("RUB");
        transactionOverNewLimitDTO.setSum(new BigDecimal("25000.00"));
        transactionOverNewLimitDTO.setExpenseCategory("service");

        Transaction transactionOverNewLimit = transactionService.processTransaction(transactionOverNewLimitDTO);
        Assertions.assertTrue(transactionOverNewLimit.isLimitExceeded());
    }
}
