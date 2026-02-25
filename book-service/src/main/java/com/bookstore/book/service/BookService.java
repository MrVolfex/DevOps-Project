package com.bookstore.book.service;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    public BookResponse createBook(BookRequest request) {
        log.info("Creating book: {}", request.getTitle());

        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("ISBN already exists: " + request.getIsbn());
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription())
                .build();

        Book saved = bookRepository.save(book);
        log.info("Book created with id: {}", saved.getId());
        return toResponse(saved);
    }

    public BookResponse getBookById(Long id) {
        log.info("Fetching book with id: {}", id);
        return bookRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
    }

    public List<BookResponse> getAllBooks() {
        log.info("Fetching all books");
        return bookRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Reaktivna pretraga koristeći RxJava Observable.
     * Demonstrira reaktivnu komunikaciju unutar servisa.
     */
    public List<BookResponse> searchBooks(String title, String author) {
        log.info("Reactive search - title: {}, author: {}", title, author);

        return Observable.fromCallable(() -> {    // ne blokira odmah
                    if (title != null && !title.isBlank()) {
                        return bookRepository.findByTitleContainingIgnoreCase(title);
                    } else if (author != null && !author.isBlank()) {
                        return bookRepository.findByAuthorContainingIgnoreCase(author);
                    } else {
                        return bookRepository.findAll();
                    }
                })
                .subscribeOn(Schedulers.io()) //radi na drugom threadu
                .map(books -> books.stream().map(this::toResponse).toList())
                .blockingFirst();
    }

    public BookResponse updateStock(Long id, int quantity) {
        log.info("Updating stock for book id: {}, delta: {}", id, quantity);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));

        int newStock = book.getStock() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock for book id: " + id);
        }
        book.setStock(newStock);
        return toResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .stock(book.getStock())
                .description(book.getDescription())
                .createdAt(book.getCreatedAt())
                .build();
    }
}
