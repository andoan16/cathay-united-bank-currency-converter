package com.cathayunitedbank.currencyconverter.service;

import com.cathayunitedbank.currencyconverter.model.Currency;
import com.cathayunitedbank.currencyconverter.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for handling business logic related to {@link Currency} entities.
 * This class acts as an intermediary between the {@link CurrencyController} and the
 * {@link CurrencyRepository}, providing methods to perform CRUD (Create, Read, Update, Delete)
 * operations on currency data. It encapsulates the data access layer details and
 * provides a clean API for currency management.
 *
 * Key Functionality:
 * - **Retrieving Currencies**: Fetches all currencies or a specific currency by its code.
 * - **Saving Currencies**: Persists new currency data or updates existing currency information.
 * - **Deleting Currencies**: Removes a currency entry from the system.
 */
@Service
public class CurrencyService {
    
    private final CurrencyRepository currencyRepository;
    
    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }
    
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAllByOrderByCodeAsc();
    }
    
    public Optional<Currency> getCurrencyByCode(String code) {
        return currencyRepository.findById(code);
    }
    
    public Currency saveCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }
    
    public void deleteCurrency(String code) {
        currencyRepository.deleteById(code);
    }
}