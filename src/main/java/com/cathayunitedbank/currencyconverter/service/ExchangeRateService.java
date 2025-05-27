package com.cathayunitedbank.currencyconverter.service;

import com.cathayunitedbank.currencyconverter.dto.ExchangeRateResponse;
import com.cathayunitedbank.currencyconverter.dto.OandaApiResponse;
import com.cathayunitedbank.currencyconverter.model.ExchangeRate;
import com.cathayunitedbank.currencyconverter.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class responsible for managing currency exchange rate data.
 * This includes fetching rates from an external API (OANDA), persisting them,
 * and providing various retrieval methods for exchange rate information.
 * It also supports scheduled synchronization and the addition of test data.
 *
 * Key Functionality:
 * - **Data Retrieval**: Provides methods to fetch all exchange rates, rates
 * by a specific base currency, or a specific rate between two currencies.
 * - **External API Integration**: Connects to the OANDA API to retrieve the
 * latest exchange rates for predefined base and quote currency pairs.
 * - **Scheduled Synchronization**: Automatically updates exchange rates daily
 * by calling the OANDA API and persisting the latest rates to the database.
 * - **Data Persistence**: Interacts with {@link ExchangeRateRepository} to save
 * and update exchange rate records.
 * - **Formatted Exchange Rate Response**: Converts raw exchange rate data into
 * a client-friendly {@link ExchangeRateResponse} DTO, including formatting
 * of the update time.
 * - **Test Data Management**: Offers a method to manually add predefined
 * test exchange rate data, useful for development and testing environments.
 *
 * Key Attributes:
 * - Uses `org.slf4j.Logger` for logging service operations and potential errors.
 * - Autowires {@link ExchangeRateRepository} for database interactions.
 * - Autowires `RestTemplate` for making HTTP calls to external APIs.
 * - Defines `BASE_CURRENCIES` and `QUOTE_CURRENCIES` arrays to specify the
 * currency pairs for which rates are synchronized.
 * - Configures a daily scheduled task (`syncExchangeRates`) to keep data fresh.
 */
@Service
public class ExchangeRateService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;
    
    private static final String[] BASE_CURRENCIES = {"EUR", "USD", "JPY"};
    private static final String[] QUOTE_CURRENCIES = {"USD", "EUR", "JPY", "GBP"};
    private static final String OANDA_API_URL = 
            "https://fxds-public-exchange-rates-api.oanda.com/cc-api/currencies?base=%s&quote=%s&data_type=chart&start_date=%s&end_date=%s";
    
    @Autowired
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, RestTemplate restTemplate) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.restTemplate = restTemplate;
    }
    
    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAll();
    }
    
    public List<ExchangeRate> getExchangeRatesByBaseCurrency(String baseCurrency) {
        return exchangeRateRepository.findByBaseCurrency(baseCurrency);
    }
    
    public ExchangeRate getExchangeRate(String baseCurrency, String quoteCurrency) {
        return exchangeRateRepository.findByBaseCurrencyAndQuoteCurrency(baseCurrency, quoteCurrency);
    }
    
    @Scheduled(fixedRate = 86400000) // Run once a day
    public void syncExchangeRates() {
        logger.info("Starting scheduled exchange rate synchronization");
        
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        for (String baseCurrency : BASE_CURRENCIES) {
            for (String quoteCurrency : QUOTE_CURRENCIES) {
                if (!baseCurrency.equals(quoteCurrency)) {
                    try {
                        updateExchangeRate(baseCurrency, quoteCurrency, firstDayOfMonth, today);
                    } catch (Exception e) {
                        logger.error("Error updating exchange rate for {}/{}: {}", 
                                baseCurrency, quoteCurrency, e.getMessage());
                    }
                }
            }
        }
        
        // Log all exchange rates after synchronization
        List<ExchangeRate> allRates = exchangeRateRepository.findAll();
        logger.info("Exchange rate synchronization completed. Current rates in database:");
        
        for (ExchangeRate rate : allRates) {
            logger.info("Rate: {} to {} = {} (updated: {})", 
                    rate.getBaseCurrency(), 
                    rate.getQuoteCurrency(), 
                    rate.getRate(),
                    rate.getUpdateTime());
        }
        
        logger.info("Total exchange rates in database: {}", allRates.size());
    }
    
    private void updateExchangeRate(String baseCurrency, String quoteCurrency, 
                                   LocalDate startDate, LocalDate endDate) {
        String url = String.format(OANDA_API_URL, 
                baseCurrency, quoteCurrency, 
                startDate.toString(), endDate.toString());
        
        logger.info("Calling OANDA API: {}", url);
        try {
            OandaApiResponse response = restTemplate.getForObject(url, OandaApiResponse.class);
            logger.info("Received response for {}/{}", baseCurrency, quoteCurrency);
            
            if (response != null && response.getResponse() != null && !response.getResponse().isEmpty()) {
                // Get the most recent data point (last in the list)
                OandaApiResponse.OandaDataPoint dataPoint = response.getResponse().get(response.getResponse().size() - 1);
                
                // Calculate the average of bid and ask as the rate
                Double averageBid = Double.parseDouble(dataPoint.getAverageBid());
                Double averageAsk = Double.parseDouble(dataPoint.getAverageAsk());
                Double rate = (averageBid + averageAsk) / 2;
                
                LocalDateTime updateTime = LocalDateTime.now();
                
                logger.info("Got rate for {}/{}: {}", baseCurrency, quoteCurrency, rate);
                
                ExchangeRate exchangeRate = exchangeRateRepository
                        .findByBaseCurrencyAndQuoteCurrency(baseCurrency, quoteCurrency);
                
                if (exchangeRate == null) {
                    exchangeRate = new ExchangeRate(baseCurrency, quoteCurrency, rate, updateTime);
                    logger.info("Creating new exchange rate record for {}/{}", baseCurrency, quoteCurrency);
                } else {
                    exchangeRate.setRate(rate);
                    exchangeRate.setUpdateTime(updateTime);
                    logger.info("Updating existing exchange rate record for {}/{}", baseCurrency, quoteCurrency);
                }
                
                ExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
                logger.info("Saved exchange rate: {} (ID: {})", savedRate, savedRate.getId());
            } else {
                logger.warn("No data received from OANDA API for {}/{}", baseCurrency, quoteCurrency);
            }
        } catch (Exception e) {
            logger.error("Error calling OANDA API for {}/{}: {}", baseCurrency, quoteCurrency, e.getMessage(), e);
        }
    }
    
    public ExchangeRateResponse getFormattedExchangeRate(String baseCurrency, String quoteCurrency) {
        ExchangeRate rate = getExchangeRate(baseCurrency, quoteCurrency);
        if (rate == null) {
            return null;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String formattedTime = rate.getUpdateTime().format(formatter);
        
        return new ExchangeRateResponse(
                formattedTime,
                rate.getBaseCurrency(),
                rate.getQuoteCurrency(),
                rate.getRate()
        );
    }

    public void addTestData() {
        logger.info("Adding test exchange rate data");
        
        try {
            // Create some test exchange rates
            ExchangeRate rate1 = new ExchangeRate("USD", "EUR", 0.85, LocalDateTime.now());
            ExchangeRate rate2 = new ExchangeRate("EUR", "USD", 1.18, LocalDateTime.now());
            ExchangeRate rate3 = new ExchangeRate("USD", "JPY", 110.5, LocalDateTime.now());
            
            // Save them to the database
            exchangeRateRepository.save(rate1);
            exchangeRateRepository.save(rate2);
            exchangeRateRepository.save(rate3);
            
            // Verify they were saved
            List<ExchangeRate> allRates = exchangeRateRepository.findAll();
            logger.info("Test data added. Current rates in database:");
            
            for (ExchangeRate rate : allRates) {
                logger.info("Rate: {} to {} = {} (updated: {})", 
                        rate.getBaseCurrency(), 
                        rate.getQuoteCurrency(), 
                        rate.getRate(),
                        rate.getUpdateTime());
            }
            
            logger.info("Total exchange rates in database after adding test data: {}", allRates.size());
        } catch (Exception e) {
            logger.error("Error adding test data: {}", e.getMessage(), e);
        }
    }
}
