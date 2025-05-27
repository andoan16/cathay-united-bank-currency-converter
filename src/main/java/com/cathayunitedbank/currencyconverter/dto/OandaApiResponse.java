package com.cathayunitedbank.currencyconverter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OandaApiResponse {
    @JsonProperty("response")
    private List<OandaDataPoint> response;
    
    public List<OandaDataPoint> getResponse() {
        return response;
    }
    
    public void setResponse(List<OandaDataPoint> response) {
        this.response = response;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OandaDataPoint {
        @JsonProperty("base_currency")
        private String baseCurrency;
        
        @JsonProperty("quote_currency")
        private String quoteCurrency;
        
        @JsonProperty("close_time")
        private String closeTime;
        
        @JsonProperty("average_bid")
        private String averageBid;
        
        @JsonProperty("average_ask")
        private String averageAsk;
        
        @JsonProperty("high_bid")
        private String highBid;
        
        @JsonProperty("high_ask")
        private String highAsk;
        
        @JsonProperty("low_bid")
        private String lowBid;
        
        @JsonProperty("low_ask")
        private String lowAsk;
        
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
        
        public String getCloseTime() {
            return closeTime;
        }
        
        public void setCloseTime(String closeTime) {
            this.closeTime = closeTime;
        }
        
        public String getAverageBid() {
            return averageBid;
        }
        
        public void setAverageBid(String averageBid) {
            this.averageBid = averageBid;
        }
        
        public String getAverageAsk() {
            return averageAsk;
        }
        
        public void setAverageAsk(String averageAsk) {
            this.averageAsk = averageAsk;
        }
        
        public String getHighBid() {
            return highBid;
        }
        
        public void setHighBid(String highBid) {
            this.highBid = highBid;
        }
        
        public String getHighAsk() {
            return highAsk;
        }
        
        public void setHighAsk(String highAsk) {
            this.highAsk = highAsk;
        }
        
        public String getLowBid() {
            return lowBid;
        }
        
        public void setLowBid(String lowBid) {
            this.lowBid = lowBid;
        }
        
        public String getLowAsk() {
            return lowAsk;
        }
        
        public void setLowAsk(String lowAsk) {
            this.lowAsk = lowAsk;
        }
    }
}
