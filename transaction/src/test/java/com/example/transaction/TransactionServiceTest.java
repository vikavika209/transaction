package com.example.transaction;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.entity.Limit;
import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import com.example.transaction.service.ExchangeRateService;
import com.example.transaction.service.LimitService;
import com.example.transaction.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    private TransactionDTO transactionOverLimit;
    private TransactionDTO transactionUnderLimit;
    private Limit limit;
    private ExchangeRate exchangeRate;
    private final Map<String, Limit> map = new HashMap<>();

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
        transactionOverLimit = new TransactionDTO();
        transactionOverLimit.setAccountFrom("0123456789");
        transactionOverLimit.setCurrencyShortname("RUB");
        transactionOverLimit.setSum(new BigDecimal("10000.00"));
        transactionOverLimit.setExpenseCategory("service");

        transactionUnderLimit = new TransactionDTO();
        transactionUnderLimit.setAccountFrom("0123456789");
        transactionUnderLimit.setCurrencyShortname("RUB");
        transactionUnderLimit.setSum(new BigDecimal("2000.00"));
        transactionUnderLimit.setExpenseCategory("service");

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

        Transaction savedTransaction = transactionService.processTransaction(transactionOverLimit);
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

        Transaction savedTransaction = transactionService.processTransaction(transactionUnderLimit);
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

        Transaction transactionOverOldLimit = transactionService.processTransaction(transactionOverLimit);
        Assertions.assertTrue(transactionOverOldLimit.isLimitExceeded());

        Limit newLimit = new Limit("0123456789", "service", new BigDecimal("200.00"), "USD");
        when(limitService.createLimit(any(LimitDTO.class))).thenReturn(newLimit);

        limitService.createLimit(new LimitDTO("0123456789", "service", new BigDecimal("200.00"), "USD"));

        when(limitService.getLimitsByAccountNumberAndCategory("0123456789", "service")).thenReturn(newLimit);

        Transaction transactionUnderNewLimit = transactionService.processTransaction(transactionOverLimit);
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
