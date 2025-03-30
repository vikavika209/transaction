package com.example.transaction.controller;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/transactions")
public class InternalController {

    Logger logger = LoggerFactory.getLogger(InternalController.class);

    private final ModelMapper modelMapper;
    private final TransactionService transactionService;

    @Autowired
    public InternalController(ModelMapper modelMapper, TransactionService transactionService) {
        this.modelMapper = modelMapper;
        this.transactionService = transactionService;
    }


    @Operation(summary = "Сохранить транзакцию",
            description = "Создает и сохраняет новую транзакцию на основе переданных данных.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Транзакция успешно сохранена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Transaction.class))}),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Ошибка на сервере")
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<TransactionDTO>> saveTransaction(@RequestBody TransactionDTO request) {
        logger.info("Получен запрос: accountFrom={}, currencyShortname={}", request.getAccountFrom(), request.getCurrencyShortname());
        return transactionService.processTransaction(request)
                .thenApply(transaction -> {
                    TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);
                    return ResponseEntity.ok(dto);
                });

    }

}

