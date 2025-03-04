package com.example.transaction.service;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionLimitDTO;
import com.example.transaction.entity.Limit;
import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    TransactionRepository transactionRepository;
    ExchangeRateService exchangeRateService;
    LimitService limitService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, LimitService limitService, ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.limitService = limitService;
        this.exchangeRateService = exchangeRateService;
    }

    private Transaction save(TransactionDTO transactionDTO) {

        Transaction transaction = transactionDTO.convertToTransaction();

        String accountFrom = transaction.getAccountFrom();
        String currency = transaction.getCurrencyShortname();
        String transactionCategory = transaction.getExpenseCategory();

        if (currency == null) {
            logger.error("Не указана валюта транзакции с id: {}", transaction.getId());
            throw new NoSuchElementException("Не указана валюта транзакции с id: " + transaction.getId());

        }

        BigDecimal transactionSumInUsd;

        if(!currency.equalsIgnoreCase("USD")){
            transactionSumInUsd = exchangeRateService.convertCurrentCurrencyInUsd(transaction.getSum(), currency);
        }else {
            transactionSumInUsd = transaction.getSum();
        }

        Limit limit = limitService.getLimitsByAccountNumberAndCategory(accountFrom, transactionCategory);

        if(limit == null){
            limit = limitService.createLimit(new LimitDTO(accountFrom, transactionCategory));
            logger.info("Создан лимит: {} для аккаунта: {} в категории {}", limit.getLimitSum(), accountFrom, transactionCategory);
        }else {

            transaction.setLimit(limit);
            logger.info("Лимит для аккаунта: {} равен {} {}.", accountFrom, limit.getLimitSum(), limit.getLimitCurrencyShortName());
        }

        BigDecimal limitOfThisAccount = Optional.ofNullable(limit.getLimitSum())
                .orElseThrow(() -> new NoSuchElementException("Лимит суммы для аккаунта " + accountFrom + " отсутствует"));

        transaction.setLimitExceeded(transactionSumInUsd.compareTo(limitOfThisAccount) > 0);
        logger.info("transactionSumInUsd = {}; limitOfThisAccount = {}", transactionSumInUsd, limitOfThisAccount);
        logger.info("transaction.isLimitExceeded() {}", transaction.isLimitExceeded());

        logger.info("Транзакция для аккаунта: {} на сумму: {} {} успешно сохранена.", transaction.getAccountFrom(), transaction.getSum(), transaction.getCurrencyShortname());

        return transactionRepository.save(transaction);
    }

    public List<TransactionLimitDTO> getTransactionsOverTheLimit(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            logger.error("Не указан номер аккаунта");
            throw new IllegalArgumentException("Не указан номер аккаунта");
        }

        return Optional.ofNullable(transactionRepository.findExceededTransactions(accountNumber))
                .orElse(Collections.emptyList());
    }

    @Async("rubExecutor")
    protected CompletableFuture<Transaction> saveRubTransaction(TransactionDTO transactionDTO) {
        return CompletableFuture.completedFuture(save(transactionDTO));
    }

    @Async("kztExecutor")
    protected CompletableFuture<Transaction> saveKztTransaction(TransactionDTO transactionDTO) {
        return CompletableFuture.completedFuture(save(transactionDTO));
    }

    public Transaction processTransaction(TransactionDTO transactionDTO) {

        if (transactionDTO.getCurrencyShortname().equalsIgnoreCase("RUB")){
            return saveRubTransaction(transactionDTO).join();
        }else if (transactionDTO.getCurrencyShortname().equalsIgnoreCase("KZT")){
            return saveKztTransaction(transactionDTO).join();
        }else {
            logger.error("Невалидная валюта транзакции с аккаунта: {}", transactionDTO.getAccountFrom());
            throw new RuntimeException("Невалидная валюта транзакции.");
        }
    }
}
