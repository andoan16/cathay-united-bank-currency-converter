package com.cathayunitedbank.currencyconverter.controller;

import com.cathayunitedbank.currencyconverter.dto.ExchangeRateResponse;
import com.cathayunitedbank.currencyconverter.model.ExchangeRate;
import com.cathayunitedbank.currencyconverter.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing currency exchange rate operations.
 * This class provides API endpoints to retrieve current exchange rates,
 * trigger synchronization with external sources, and add test data.
 * All endpoints are accessible under the `/api/exchange-rates` base path.
 *
 * Key Functionality:
 * - **Retrieve All Exchange Rates**: Fetches all available exchange rates stored in the database.
 * - **Retrieve Exchange Rates by Base Currency**: Retrieves a list of all exchange rates originating from a specific base currency.
 * - **Retrieve Specific Exchange Rate**: Gets the exchange rate between a designated base currency and a quote currency, returning a formatted response.
 * - **Synchronize Exchange Rates**: Initiates a manual process to update exchange rates from an external API.
 * - **Add Test Data**: Allows for the insertion of predefined test exchange rate data into the system, useful for development and testing environments.
 *
 * This controller delegates business logic to the {@link ExchangeRateService}.
 */
@RestController
@RequestMapping("/api/exchange-rates")
@Tag(name = "Exchange Rate Management", description = "APIs for managing currency exchange rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    @Operation(summary = "Get all exchange rates", description = "Retrieves all exchange rates from the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeRate.class)))
    })
    public ResponseEntity<List<ExchangeRate>> getAllExchangeRates() {
        return ResponseEntity.ok(exchangeRateService.getAllExchangeRates());
    }

    @GetMapping("/{baseCurrency}")
    @Operation(summary = "Get exchange rates by base currency", 
               description = "Retrieves all exchange rates for a specific base currency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeRate.class))),
        @ApiResponse(responseCode = "404", description = "No exchange rates found for the base currency", 
                content = @Content)
    })
    public ResponseEntity<List<ExchangeRate>> getExchangeRatesByBaseCurrency(
            @Parameter(description = "Base currency code (e.g., USD, EUR)", required = true)
            @PathVariable String baseCurrency) {
        List<ExchangeRate> rates = exchangeRateService.getExchangeRatesByBaseCurrency(baseCurrency);
        if (rates.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/{baseCurrency}/{quoteCurrency}")
    @Operation(summary = "Get specific exchange rate", 
               description = "Retrieves the exchange rate between two currencies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the exchange rate",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeRateResponse.class))),
        @ApiResponse(responseCode = "404", description = "Exchange rate not found", content = @Content)
    })
    public ResponseEntity<ExchangeRateResponse> getExchangeRate(
            @Parameter(description = "Base currency code (e.g., USD, EUR)", required = true)
            @PathVariable String baseCurrency,
            @Parameter(description = "Quote currency code (e.g., USD, EUR)", required = true)
            @PathVariable String quoteCurrency) {
        ExchangeRateResponse response = exchangeRateService.getFormattedExchangeRate(
                baseCurrency.toUpperCase(), 
                quoteCurrency.toUpperCase());
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    @Operation(summary = "Synchronize exchange rates", 
               description = "Manually triggers the synchronization of exchange rates with external API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronization process started successfully", 
                content = @Content)
    })
    public ResponseEntity<Void> syncExchangeRates() {
        exchangeRateService.syncExchangeRates();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-data")
    @Operation(summary = "Add test data", 
               description = "Adds test exchange rate data to the database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test data added successfully", 
                content = @Content)
    })
    public ResponseEntity<Void> addTestData() {
        exchangeRateService.addTestData();
        return ResponseEntity.ok().build();
    }
}
