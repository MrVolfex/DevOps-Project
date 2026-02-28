package com.bookstore.book.service;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book buildBook() {
        return Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("978-0132350884")
                .price(new BigDecimal("39.99"))
                .stock(10)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private BookRequest buildRequest() {
        BookRequest req = new BookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert Martin");
        req.setIsbn("978-0132350884");
        req.setPrice(new BigDecimal("39.99"));
        req.setStock(10);
        return req;
    }

    @Test
    void createBook_success() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(buildBook());

        BookResponse response = bookService.createBook(buildRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getPrice()).isEqualByComparingTo("39.99");
    }

    @Test
    void createBook_duplicateIsbn_throwsException() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(buildRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISBN already exists");
    }

    @Test
    void getBookById_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(buildBook()));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void getBookById_notFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void getAllBooks_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(buildBook(), buildBook()));

        List<BookResponse> result = bookService.getAllBooks();

        assertThat(result).hasSize(2);
    }

    // RxJava: Observable.fromCallable + Schedulers.io() + blockingFirst()
    // Mockito direktno mokuje repozitorijum — nema potrebe za RxJava setup-om
    @Test
    void searchBooks_byTitle_returnsMatchingBooks() {
        when(bookRepository.findByTitleContainingIgnoreCase("Clean"))
                .thenReturn(List.of(buildBook()));

        List<BookResponse> result = bookService.searchBooks("Clean", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void searchBooks_byAuthor_returnsMatchingBooks() {
        when(bookRepository.findByAuthorContainingIgnoreCase("Martin"))
                .thenReturn(List.of(buildBook()));

        List<BookResponse> result = bookService.searchBooks(null, "Martin");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Robert Martin");
    }

    @Test
    void searchBooks_noFilter_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(buildBook(), buildBook()));

        List<BookResponse> result = bookService.searchBooks(null, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void updateStock_success() {
        Book book = buildBook(); // stock = 10
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.updateStock(1L, -3);

        assertThat(response).isNotNull();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateStock_insufficientStock_throwsException() {
        Book book = buildBook(); // stock = 10
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // pokušavamo uzeti 20 od stock-a 10 — treba da baci grešku
        assertThatThrownBy(() -> bookService.updateStock(1L, -20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void updateStock_notFound_throwsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateStock(99L, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void deleteBook_success() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_notFound_throwsException() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book not found");
    }
}
