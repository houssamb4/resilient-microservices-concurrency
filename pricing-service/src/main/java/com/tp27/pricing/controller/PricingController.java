package com.tp27.pricing.controller;

import com.tp27.pricing.model.PriceResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Random;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {
    
    private final Random random = new Random();
    
    @GetMapping("/{bookId}")
    public PriceResponse getPrice(@PathVariable Long bookId) {
        // Simulate some processing time
        try {
            Thread.sleep(50 + random.nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Return a random price between 5.0 and 25.0
        double price = 5.0 + (random.nextDouble() * 20.0);
        return new PriceResponse(bookId, Math.round(price * 100.0) / 100.0);
    }
}
