package com.cathayunitedbank.currencyconverter;

import com.cathayunitedbank.currencyconverter.controller.ExchangeRateController;
import com.cathayunitedbank.currencyconverter.dto.ExchangeRateResponse;
import com.cathayunitedbank.currencyconverter.model.ExchangeRate;
import com.cathayunitedbank.currencyconverter.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the {@link ExchangeRateController} class.
 * This class uses Spring Boot's {@link WebMvcTest} to thoroughly test the web layer,
 * focusing on the HTTP endpoints provided by `ExchangeRateController`. It isolates
 * the controller by mocking the `ExchangeRateService` dependency, allowing for precise
 * control over service behavior and verification of interactions.
 *
 * Key Functionality Tested:
 * - **`GET /api/exchange-rates`**: Verifies the retrieval of all exchange rates, ensuring
 * an OK status and the correct JSON structure for the list of rates.
 * - **`GET /api/exchange-rates/{baseCurrency}`**: Tests fetching exchange rates for a
 * specific base currency, including successful retrieval and handling of cases where
 * no rates are found (returning a 404 Not Found status).
 * - **`GET /api/exchange-rates/{baseCurrency}/{quoteCurrency}`**: Checks the retrieval
 * of a specific exchange rate between two currencies. This includes asserting the correct
 * `ExchangeRateResponse` data and verifying the 404 Not Found response when the rate
 * is not available.
 * - **`POST /api/exchange-rates/sync`**: Confirms that the synchronization endpoint
 * correctly triggers the exchange rate synchronization process in the service and
 * returns an OK status.
 * - **`POST /api/exchange-rates/test-data`**: Asserts that the test data endpoint
 * successfully initiates the addition of test exchange rates and returns an OK status.
 *
 * Key Attributes:
 * - Employs `MockMvc` for simulating HTTP requests and validating the responses,
 * including HTTP status codes and JSON content using `jsonPath`.
 * - Utilizes `@MockBean` to provide a mock instance of `ExchangeRateService`,
 * allowing control over its methods' return values and verification of their invocations.
 * - Initializes `ExchangeRate` and `ExchangeRateResponse` objects in a `@BeforeEach`
 * setup method to create consistent test data for various scenarios.
 * - Handles `Double` to `BigDecimal` conversion for rates, ensuring consistency
 * with typical financial data handling in the service layer.
 */
@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    private ExchangeRate usdEurRate;
    private ExchangeRate usdJpyRate;
    private ExchangeRateResponse usdEurResponse;

    @SuppressWarnings("removal")
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        usdEurRate = new ExchangeRate("USD", "EUR", new Double("0.92"), now);
        usdJpyRate = new ExchangeRate("USD", "JPY", new Double("156.45"), now);

        usdEurResponse = new ExchangeRateResponse(now.toString(), "USD", "EUR", new Double("0.92"));
    }

    @Test
    void getAllExchangeRates_shouldReturnListOfExchangeRates() throws Exception {
        // Given
        List<ExchangeRate> rates = Arrays.asList(usdEurRate, usdJpyRate);
        when(exchangeRateService.getAllExchangeRates()).thenReturn(rates);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].baseCurrency").value("USD"))
                .andExpect(jsonPath("$[0].quoteCurrency").value("EUR"))
                .andExpect(jsonPath("$[1].baseCurrency").value("USD"))
                .andExpect(jsonPath("$[1].quoteCurrency").value("JPY"));

        verify(exchangeRateService, times(1)).getAllExchangeRates();
    }

    @Test
    void getExchangeRatesByBaseCurrency_shouldReturnListOfExchangeRates_whenFound() throws Exception {
        // Given
        List<ExchangeRate> usdRates = Arrays.asList(usdEurRate, usdJpyRate);
        when(exchangeRateService.getExchangeRatesByBaseCurrency("USD")).thenReturn(usdRates);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/{baseCurrency}", "USD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].baseCurrency").value("USD"))
                .andExpect(jsonPath("$[0].quoteCurrency").value("EUR"))
                .andExpect(jsonPath("$[1].baseCurrency").value("USD"))
                .andExpect(jsonPath("$[1].quoteCurrency").value("JPY"));

        verify(exchangeRateService, times(1)).getExchangeRatesByBaseCurrency("USD");
    }

    @Test
    void getExchangeRatesByBaseCurrency_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        when(exchangeRateService.getExchangeRatesByBaseCurrency("XYZ")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/{baseCurrency}", "XYZ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(exchangeRateService, times(1)).getExchangeRatesByBaseCurrency("XYZ");
    }

    @Test
    void getExchangeRate_shouldReturnExchangeRateResponse_whenFound() throws Exception {
        // Given
        when(exchangeRateService.getFormattedExchangeRate("USD", "EUR")).thenReturn(usdEurResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/{baseCurrency}/{quoteCurrency}", "USD", "EUR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.quoteCurrency").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.92));

        verify(exchangeRateService, times(1)).getFormattedExchangeRate("USD", "EUR");
    }

    @Test
    void getExchangeRate_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        when(exchangeRateService.getFormattedExchangeRate("USD", "XYZ")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/{baseCurrency}/{quoteCurrency}", "USD", "XYZ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(exchangeRateService, times(1)).getFormattedExchangeRate("USD", "XYZ");
    }

    @Test
    void syncExchangeRates_shouldReturnOk() throws Exception {
        // Given
        doNothing().when(exchangeRateService).syncExchangeRates();

        // When & Then
        mockMvc.perform(post("/api/exchange-rates/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(exchangeRateService, times(1)).syncExchangeRates();
    }

    @Test
    void addTestData_shouldReturnOk() throws Exception {
        // Given
        doNothing().when(exchangeRateService).addTestData();

        // When & Then
        mockMvc.perform(post("/api/exchange-rates/test-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(exchangeRateService, times(1)).addTestData();
    }
}