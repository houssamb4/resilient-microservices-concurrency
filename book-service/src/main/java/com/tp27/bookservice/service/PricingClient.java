package com.tp27.bookservice.service;

import com.tp27.bookservice.model.PriceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PricingClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PricingClient.class);
    
    private final RestTemplate restTemplate;
    private final String pricingServiceUrl;
    
    public PricingClient(RestTemplate restTemplate,
                        @Value("${pricing.service.url}") String pricingServiceUrl) {
        this.restTemplate = restTemplate;
        this.pricingServiceUrl = pricingServiceUrl;
    }
    
    @Retry(name = "pricingService", fallbackMethod = "getPriceFallback")
    @CircuitBreaker(name = "pricingService", fallbackMethod = "getPriceFallback")
    public Double getPrice(Long bookId) {
        logger.info("Calling pricing service for book {}", bookId);
        String url = pricingServiceUrl + "/api/pricing/" + bookId;
        PriceResponse response = restTemplate.getForObject(url, PriceResponse.class);
        return response != null ? response.getPrice() : 0.0;
    }
    
    private Double getPriceFallback(Long bookId, Exception ex) {
        logger.warn("Pricing service unavailable for book {}. Using fallback. Error: {}", 
                   bookId, ex.getMessage());
        return 0.0;
    }
}
