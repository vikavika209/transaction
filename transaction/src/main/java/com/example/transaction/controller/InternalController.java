package com.example.transaction.controller;

import com.example.transaction.dto.TransactionDTO;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class InternalController {
    private final TransactionService transactionService;

    @Autowired
    public InternalController(TransactionService transactionService) {
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
    public ResponseEntity<Transaction> saveTransaction(@RequestBody TransactionDTO request) {
        Transaction transaction = transactionService.save(request);
        return ResponseEntity.ok(transaction);
    }

}
