package com.cathayunitedbank.currencyconverter.dto;

public class ExchangeRateResponse {
    private String updateTime;
    private String baseCurrency;
    private String quoteCurrency;
    private Double rate;
    
    public ExchangeRateResponse() {}
    
    public ExchangeRateResponse(String updateTime, String baseCurrency, String quoteCurrency, Double rate) {
        this.updateTime = updateTime;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.rate = rate;
    }
    
    public String getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getBaseCurrency() {
        return baseCurrency;
    }
    
    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
    
    public String getQuoteCurrency() {
        return quoteCurrency;
    }
    
    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }
    
    public Double getRate() {
        return rate;
    }
    
    public void setRate(Double rate) {
        this.rate = rate;
    }
}