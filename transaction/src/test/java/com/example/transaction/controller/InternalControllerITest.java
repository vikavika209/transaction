package com.example.transaction.controller;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.entity.Limit;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.ExchangeRateService;
import com.example.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class InternalControllerITest {

    private ExchangeRate exchangeRate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    ExchangeRateService exchangeRateService;

    @Spy
    ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        exchangeRate = new ExchangeRate();
        exchangeRate.setCurrency("RUB");
        exchangeRate.setRate(BigDecimal.valueOf(100.00));
    }

    @Test
    void testSaveTransaction() throws Exception {
        TransactionDTO request = new TransactionDTO("0123456789", "9876543210", "RUB", new BigDecimal("1000.00"), "product");
        Transaction response = modelMapper.map(request, Transaction.class);

        when(transactionService.processTransaction(any(TransactionDTO.class))).thenReturn(CompletableFuture.completedFuture(response));
        when(exchangeRateService.convertCurrentCurrencyInUsd(any(BigDecimal.class), any(String.class)))
                .thenAnswer(invocation -> {
                    BigDecimal sum = invocation.getArgument(0);
                    return sum.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                });

        MvcResult mvcResult = mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountFrom").value("0123456789"))
                .andExpect(jsonPath("$.sum").value(1000.00))
                .andExpect(jsonPath("$.expenseCategory").value("product"));

    }
}
