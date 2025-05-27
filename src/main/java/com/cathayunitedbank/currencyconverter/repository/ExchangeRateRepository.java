package com.cathayunitedbank.currencyconverter.repository;

import com.cathayunitedbank.currencyconverter.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findByBaseCurrency(String baseCurrency);
    ExchangeRate findByBaseCurrencyAndQuoteCurrency(String baseCurrency, String quoteCurrency);
}