package com.tp27.bookservice.model;

public class PriceResponse {
    private Long bookId;
    private Double price;
    
    public PriceResponse() {}
    
    public PriceResponse(Long bookId, Double price) {
        this.bookId = bookId;
        this.price = price;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
}
