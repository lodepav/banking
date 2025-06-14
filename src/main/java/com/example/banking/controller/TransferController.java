package com.example.banking.controller;

import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.TransferResponse;
import com.example.banking.dto.TransferResult;
import com.example.banking.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Transfer controller.
 */
@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Transfer funds response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @Operation(
            summary = "Transfer funds",
            description = "Transfer funds between accounts with currency conversion",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer successful"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid transfer request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Business rule violation",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<TransferResponse> transferFunds(
            @Valid @RequestBody TransferRequest request
    ) {
        TransferResult result = transferService.transferFunds(request);
        return ResponseEntity.ok(TransferResponse.fromTransferResult(result));
    }
}