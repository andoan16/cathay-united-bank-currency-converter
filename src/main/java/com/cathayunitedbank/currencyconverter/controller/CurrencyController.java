package com.cathayunitedbank.currencyconverter.controller;

import com.cathayunitedbank.currencyconverter.model.Currency;
import com.cathayunitedbank.currencyconverter.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing currency-related operations.
 * This class provides a comprehensive set of API endpoints for retrieving,
 * creating, updating, and deleting currency information.
 * All endpoints are prefixed with `/api/currencies`.
 *
 * Key Functionality:
 * - **Retrieve All Currencies**: Fetches a sorted list of all available currencies.
 * - **Retrieve Currency by Code**: Retrieves details for a specific currency using its unique code.
 * - **Create Currency**: Adds a new currency entry to the system.
 * - **Update Currency**: Modifies an existing currency's details based on its code.
 * - **Delete Currency**: Removes a currency entry from the system.
 *
 * This controller interacts with the {@link CurrencyService} to perform business logic
 * and data persistence operations. It's designed to be the primary interface for
 * client applications needing to manage currency data.
 */
@RestController
@RequestMapping("/api/currencies")
@Tag(name = "Currency Management", description = "APIs for managing currency information")
public class CurrencyController {
    
    private final CurrencyService currencyService;
    
    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }
    
    @GetMapping
    @Operation(summary = "Get all currencies", description = "Retrieves a list of all available currencies sorted by code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of currencies",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Currency.class)))
    })
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }
    
    @GetMapping("/{code}")
    @Operation(summary = "Get currency by code", description = "Retrieves a specific currency by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the currency",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Currency.class))),
        @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
    })
    public ResponseEntity<Currency> getCurrencyByCode(
            @Parameter(description = "Currency code (e.g., USD, EUR)", required = true)
            @PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create a new currency", description = "Creates a new currency entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Currency successfully created",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Currency.class)))
    })
    public ResponseEntity<Currency> createCurrency(
            @Parameter(description = "Currency details", required = true)
            @RequestBody Currency currency) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(currencyService.saveCurrency(currency));
    }
    
    @PutMapping("/{code}")
    @Operation(summary = "Update a currency", description = "Updates an existing currency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Currency successfully updated",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Currency.class))),
        @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
    })
    public ResponseEntity<Currency> updateCurrency(
            @Parameter(description = "Currency code to update", required = true)
            @PathVariable String code, 
            @Parameter(description = "Updated currency details", required = true)
            @RequestBody Currency currency) {
        
        return currencyService.getCurrencyByCode(code)
                .map(existingCurrency -> {
                    currency.setCode(code);
                    return ResponseEntity.ok(currencyService.saveCurrency(currency));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{code}")
    @Operation(summary = "Delete a currency", description = "Deletes an existing currency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Currency successfully deleted", content = @Content),
        @ApiResponse(responseCode = "404", description = "Currency not found", content = @Content)
    })
    public ResponseEntity<Void> deleteCurrency(
            @Parameter(description = "Currency code to delete", required = true)
            @PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(currency -> {
                    currencyService.deleteCurrency(code);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
