package com.example.transaction.controller;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.entity.ExchangeRate;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.LimitService;
import com.example.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class ClientControllerTest {

    private ExchangeRate exchangeRate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LimitService limitService;

    @Autowired
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        limitService.deleteAllLimits();
        transactionService.deleteAllTransactions();

        Transaction transaction = new Transaction();
        transaction.setAccountFrom("0123456789");
        transaction.setAccountTo("9876543210");
        transaction.setSum(new BigDecimal("500.00"));
        transaction.setCurrencyShortname("RUB");
        transaction.setExpenseCategory("product");
        transactionService.processTransaction(new TransactionDTO(transaction));
    }

    @Test
    @Transactional
    void returnExceededTransactions() throws Exception {
        String accountNumber = "0123456789";

        mockMvc.perform(get("/api/client/transactions/exceeded/{accountNumber}", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Transactional
    void createLimit() throws Exception {
        LimitDTO limitDTO = new LimitDTO("0123456789", "product", new BigDecimal("5000.00"), "RUB");

        mockMvc.perform(post("/api/client/limits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(limitDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account").value("0123456789"))
                .andExpect(jsonPath("$.limitCurrencyShortName").value("USD"));
    }

    @Test
    @Transactional
    void getLimits() throws Exception {
        String accountNumber = "9876543210";

        mockMvc.perform(get("/api/client/limits/{accountNumber}", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.product.account").value("9876543210"))
                .andExpect(jsonPath("$.product.limitSum").value(1000));

        limitService.createLimit(new LimitDTO(accountNumber, "product", new BigDecimal("5000.00"), "RUB"));

        mockMvc.perform(get("/api/client/limits/{accountNumber}", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$.product.account").value("9876543210"))
                .andExpect(jsonPath("$.product.limitCurrencyShortName").value("USD"));
    }
}
