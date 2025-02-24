package com.example.transaction.controller;

import com.example.transaction.dto.LimitDTO;
import com.example.transaction.dto.TransactionLimitDTO;
import com.example.transaction.entity.Limit;
import com.example.transaction.service.LimitService;
import com.example.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final TransactionService transactionService;
    private final LimitService limitService;

    @Autowired
    public ClientController(TransactionService transactionService, LimitService limitService) {
        this.transactionService = transactionService;
        this.limitService = limitService;
    }

    @Operation(summary = "Получить список транзакций, превышающих лимит",
            description = "Возвращает список транзакций, которые превысили установленный лимит по счету.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список превышенных транзакций найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionLimitDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Транзакции не найдены")
    })
    @GetMapping("/transactions/exceeded/{accountNumber}")
    public ResponseEntity<List<TransactionLimitDTO>> getExceededTransactions(@PathVariable String accountNumber) {
        List<TransactionLimitDTO> transactions = transactionService.getTransactionsOverTheLimit(accountNumber);
        return ResponseEntity.ok(transactions);
    }


    @Operation(summary = "Создать лимит для счёта",
            description = "Добавляет новый лимит для заданного счета.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Лимит успешно создан",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Limit.class))}),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PostMapping("/limits")
    public ResponseEntity<Limit> createLimit(@RequestBody LimitDTO request) {
        Limit limit = limitService.createLimit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(limit);
    }


    @Operation(summary = "Получить лимиты по номеру счета",
            description = "Возвращает все лимиты, установленные для данного номера счета.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лимиты найдены",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Лимиты не найдены")
    })
    @GetMapping("/limits/{accountNumber}")
    public ResponseEntity<Map<String, Limit>> getLimits(@PathVariable String accountNumber) {
        Map<String, Limit> limits = limitService.getLimitsByAccountNumber(accountNumber);
        return ResponseEntity.ok(limits);
    }
}
