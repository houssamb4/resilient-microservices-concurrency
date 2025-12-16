package com.tp27.bookservice.service;

import com.tp27.bookservice.exception.BookNotFoundException;
import com.tp27.bookservice.exception.OutOfStockException;
import com.tp27.bookservice.model.Book;
import com.tp27.bookservice.model.BorrowResponse;
import com.tp27.bookservice.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    private final BookRepository bookRepository;
    private final PricingClient pricingClient;
    
    public BookService(BookRepository bookRepository, PricingClient pricingClient) {
        this.bookRepository = bookRepository;
        this.pricingClient = pricingClient;
    }
    
    public Book createBook(Book book) {
        logger.info("Creating book: {}", book.getTitle());
        return bookRepository.save(book);
    }
    
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }
    
    @Transactional
    public BorrowResponse borrowBook(Long bookId) {
        logger.info("Attempting to borrow book with id: {}", bookId);
        
        // Use pessimistic lock to prevent concurrent stock issues
        Book book = bookRepository.findByIdForUpdate(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + bookId));
        
        if (book.getStock() <= 0) {
            logger.warn("Book {} is out of stock", bookId);
            throw new OutOfStockException("Book is out of stock");
        }
        
        // Decrement stock
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);
        
        // Get price from pricing service
        Double price = pricingClient.getPrice(bookId);
        
        logger.info("Book {} borrowed successfully. Stock left: {}", bookId, book.getStock());
        
        return new BorrowResponse(book.getId(), book.getTitle(), book.getStock(), price);
    }
}
