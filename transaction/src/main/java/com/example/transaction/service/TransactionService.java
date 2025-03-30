package com.example.transaction.service;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.dto.TransactionLimitDTO;
import com.example.transaction.entity.Limit;
import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;
    TransactionRepository transactionRepository;
    ExchangeRateService exchangeRateService;
    LimitService limitService;

    @Autowired
    public TransactionService(ModelMapper modelMapper, TransactionRepository transactionRepository, LimitService limitService, ExchangeRateService exchangeRateService) {
        this.modelMapper = modelMapper;
        this.transactionRepository = transactionRepository;
        this.limitService = limitService;
        this.exchangeRateService = exchangeRateService;
    }

    private Transaction save(TransactionDTO transactionDTO) {

        //Мап ДТО в транзакцию
        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);

        //Объявление переменных
        String accountFrom = transaction.getAccountFrom();
        String transactionCategory = transaction.getExpenseCategory();

        //Конвертация суммы транзакции в USD
        BigDecimal transactionSumInUsd = convertTransactionSumInUsd(transaction);

        //Получение лимита
        Limit limit = findOrCreateLimitForAccountByCategory(accountFrom, transactionCategory);
        transaction.setLimit(limit);
        BigDecimal limitOfThisAccount = limit.getLimitSum();
        logger.info("Лимит для аккаунта: {} равен {} {}.", accountFrom, limitOfThisAccount, limit.getLimitCurrencyShortName());

        //Установка флага превышения лимита
        transaction.setLimitExceeded(transactionSumInUsd.compareTo(limitOfThisAccount) > 0);
        logger.info("transactionSumInUsd = {}; limitOfThisAccount = {}", transactionSumInUsd, limitOfThisAccount);
        logger.info("transaction.isLimitExceeded() {}", transaction.isLimitExceeded());

        //Сохранение транзакции
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
    public CompletableFuture<Transaction> saveRubTransaction(TransactionDTO transactionDTO) {
        return CompletableFuture.completedFuture(save(transactionDTO));
    }

    @Async("kztExecutor")
    public CompletableFuture<Transaction> saveKztTransaction(TransactionDTO transactionDTO) {
        return CompletableFuture.completedFuture(save(transactionDTO));
    }

    @Async
    public CompletableFuture<Transaction> processTransaction(TransactionDTO transactionDTO) {

        if (transactionDTO.getCurrencyShortname().equalsIgnoreCase("RUB")){
            return saveRubTransaction(transactionDTO);
        }else if (transactionDTO.getCurrencyShortname().equalsIgnoreCase("KZT")){
            return saveKztTransaction(transactionDTO);
        }else {
            logger.error("Невалидная валюта транзакции с аккаунта: {}", transactionDTO.getAccountFrom());
            throw new RuntimeException("Невалидная валюта транзакции.");
        }
    }

    private Limit findOrCreateLimitForAccountByCategory(String account, String category) {
        Limit limit = limitService.getLimitsByAccountNumberAndCategory(account, category);

        if (limit == null) {
            limit = limitService.createLimit(new LimitDTO(account, category));
            logger.info("Создан лимит: {} для аккаунта: {} в категории {}", limit.getLimitSum(), account, category);
        }

        return limit;
    }

    private BigDecimal convertTransactionSumInUsd(Transaction transaction) {
        if (transaction.getCurrencyShortname() == null) {
            logger.error("Не указана валюта транзакции с id: {}", transaction.getId());
            throw new NoSuchElementException("Не указана валюта транзакции с id: " + transaction.getId());
        }

        BigDecimal transactionSumInUsd;
        if(!transaction.getCurrencyShortname().equalsIgnoreCase("USD")){
            transactionSumInUsd = exchangeRateService.convertCurrentCurrencyInUsd(transaction.getSum(), transaction.getCurrencyShortname());
        }else {
            transactionSumInUsd = transaction.getSum();
        }
        return transactionSumInUsd;
    }
}
