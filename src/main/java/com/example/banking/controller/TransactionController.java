package com.example.banking.controller;

import com.example.banking.domain.AccountTransaction;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Get account transactions",
            description = "Returns transaction history for an account with pagination",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated transaction list",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid account ID or pagination parameters"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found"
                    )
            }
    )
                    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @Parameter(description = "Account identifier", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable @NotNull UUID accountId,
            @Parameter(description = "Pagination offset", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Maximum results per page", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<AccountTransaction> transactions = transactionService.getAccountTransactions(accountId, pageable);
        List<TransactionResponse> response = transactions.stream()
                .map(TransactionResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }
}
