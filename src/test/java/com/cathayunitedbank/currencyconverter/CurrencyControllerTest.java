package com.cathayunitedbank.currencyconverter;

import com.cathayunitedbank.currencyconverter.controller.CurrencyController;
import com.cathayunitedbank.currencyconverter.model.Currency;
import com.cathayunitedbank.currencyconverter.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the {@link CurrencyController} class.
 * This class uses Spring Boot's {@link WebMvcTest} to focus on testing the web layer,
 * specifically the HTTP endpoints exposed by `CurrencyController`, without starting a full
 * Spring application context or requiring a real database connection.
 *
 * It employs Mockito to mock the `CurrencyService` dependency, allowing isolation of the
 * controller's logic and precise control over the service's behavior during tests.
 *
 * Key Functionality Tested:
 * - **`GET /api/currencies`**: Verifies that retrieving all currencies returns an OK status
 * and the expected list of currency objects.
 * - **`GET /api/currencies/{code}`**: Checks both successful retrieval of a currency by code
 * and the appropriate 404 Not Found response when a currency does not exist.
 * - **`POST /api/currencies`**: Ensures that creating a new currency returns a 201 Created status
 * and the details of the newly created currency.
 * - **`PUT /api/currencies/{code}`**: Tests the update operation for an existing currency,
 * including successful update and handling of non-existent currencies (404 Not Found).
 * - **`DELETE /api/currencies/{code}`**: Verifies the successful deletion of a currency (204 No Content)
 * and the 404 Not Found response for attempts to delete non-existent currencies.
 *
 * Key Attributes:
 * - Uses `MockMvc` to simulate HTTP requests and assert on responses.
 * - Leverages `@MockBean` to inject a mocked instance of `CurrencyService`.
 * - Employs `ObjectMapper` for JSON serialization/deserialization in request bodies.
 * - Sets up common test data (`usd`, `eur`) in a `@BeforeEach` method for reusability.
 */
@WebMvcTest(CurrencyController.class) // Focuses testing on CurrencyController
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc; // Used to simulate HTTP requests

    @MockBean // Creates a Mockito mock and registers it as a Spring bean
    private CurrencyService currencyService;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON and vice-versa

    private Currency usd;
    private Currency eur;

    @BeforeEach
    void setUp() {
        usd = new Currency("USD", "United States Dollar");
        eur = new Currency("EUR", "Euro");
    }

    @Test
    void getAllCurrencies_shouldReturnListOfCurrencies() throws Exception {
        // Given
        List<Currency> currencies = Arrays.asList(usd, eur);
        when(currencyService.getAllCurrencies()).thenReturn(currencies);

        // When & Then
        mockMvc.perform(get("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("USD"))
                .andExpect(jsonPath("$[0].name").value("United States Dollar"))
                .andExpect(jsonPath("$[1].code").value("EUR"))
                .andExpect(jsonPath("$[1].name").value("Euro"));

        verify(currencyService, times(1)).getAllCurrencies(); // Verify service method was called
    }

    @Test
    void getCurrencyByCode_shouldReturnCurrency_whenFound() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("USD")).thenReturn(Optional.of(usd));

        // When & Then
        mockMvc.perform(get("/api/currencies/{code}", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD"))
                .andExpect(jsonPath("$.name").value("United States Dollar"));

        verify(currencyService, times(1)).getCurrencyByCode("USD");
    }

    @Test
    void getCurrencyByCode_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("XYZ")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/currencies/{code}", "XYZ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(currencyService, times(1)).getCurrencyByCode("XYZ");
    }

    @Test
    void createCurrency_shouldReturnCreatedCurrency() throws Exception {
        // Given
        Currency newCurrency = new Currency("JPY", "Japanese Yen");
        when(currencyService.saveCurrency(any(Currency.class))).thenReturn(newCurrency);

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCurrency))) // Convert object to JSON string
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("JPY"))
                .andExpect(jsonPath("$.name").value("Japanese Yen"));

        verify(currencyService, times(1)).saveCurrency(any(Currency.class));
    }

    @Test
    void updateCurrency_shouldReturnUpdatedCurrency_whenFound() throws Exception {
        // Given
        Currency updatedUsd = new Currency("USD", "Updated US Dollar");
        when(currencyService.getCurrencyByCode("USD")).thenReturn(Optional.of(usd)); // Simulate finding the existing one
        when(currencyService.saveCurrency(any(Currency.class))).thenReturn(updatedUsd); // Simulate saving the updated one

        // When & Then
        mockMvc.perform(put("/api/currencies/{code}", "USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUsd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD"))
                .andExpect(jsonPath("$.name").value("Updated US Dollar"));

        verify(currencyService, times(1)).getCurrencyByCode("USD");
        verify(currencyService, times(1)).saveCurrency(any(Currency.class));
    }

    @Test
    void updateCurrency_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Currency nonExistentCurrency = new Currency("XYZ", "Non Existent");
        when(currencyService.getCurrencyByCode("XYZ")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/currencies/{code}", "XYZ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentCurrency)))
                .andExpect(status().isNotFound());

        verify(currencyService, times(1)).getCurrencyByCode("XYZ");
        verify(currencyService, never()).saveCurrency(any(Currency.class)); // Ensure save was not called
    }

    @Test
    void deleteCurrency_shouldReturnNoContent_whenFound() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("USD")).thenReturn(Optional.of(usd));
        doNothing().when(currencyService).deleteCurrency("USD"); // Mock void method

        // When & Then
        mockMvc.perform(delete("/api/currencies/{code}", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(currencyService, times(1)).getCurrencyByCode("USD");
        verify(currencyService, times(1)).deleteCurrency("USD");
    }

    @Test
    void deleteCurrency_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("XYZ")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/currencies/{code}", "XYZ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(currencyService, times(1)).getCurrencyByCode("XYZ");
        verify(currencyService, never()).deleteCurrency(anyString()); // Ensure delete was not called
    }
}