package com.tp27.bookservice.model;

public class BorrowResponse {
    private Long bookId;
    private String title;
    private Integer stockLeft;
    private Double price;
    
    public BorrowResponse() {}
    
    public BorrowResponse(Long bookId, String title, Integer stockLeft, Double price) {
        this.bookId = bookId;
        this.title = title;
        this.stockLeft = stockLeft;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Integer getStockLeft() {
        return stockLeft;
    }
    
    public void setStockLeft(Integer stockLeft) {
        this.stockLeft = stockLeft;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
}
