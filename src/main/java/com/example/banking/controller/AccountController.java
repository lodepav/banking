package com.example.banking.controller;

import com.example.banking.domain.Account;
import com.example.banking.dto.AccountResponse;
import com.example.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The type Account controller.
 */
@Validated
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Gets client accounts.
     *
     * @param clientId the client id
     * @return the client accounts
     */
    @Operation(
            summary = "Get client accounts",
            description = "Returns all accounts for a given client identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of client accounts",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid client ID format"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Client not found"
                    )
            }
    )
                    @GetMapping("/{clientId}/accounts")
    public ResponseEntity<List<AccountResponse>> getClientAccounts(
            @Parameter(description = "Client identifier", example = "client-123")
            @PathVariable @NotBlank String clientId
    ) {
        List<Account> accounts = accountService.getClientAccounts(clientId);
        List<AccountResponse> response = accounts.stream()
                .map(AccountResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }
}