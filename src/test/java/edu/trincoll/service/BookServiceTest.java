package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService")
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @InjectMocks private BookService bookService;

    @Test
    @DisplayName("checkoutBook updates status, due date, and persists")
    void checkoutBook_updatesState() {
        Book book = new Book();
        book.setStatus(BookStatus.AVAILABLE);
        Member member = new Member();
        member.setEmail("reader@example.com");

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookService.checkoutBook(book, member, 7);

        assertThat(book.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);
        assertThat(book.getCheckedOutBy()).isEqualTo("reader@example.com");
        assertThat(book.getDueDate()).isEqualTo(LocalDate.now().plusDays(7));
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("checkoutBook throws when book unavailable")
    void checkoutBook_rejectsUnavailable() {
        Book book = new Book();
        book.setStatus(BookStatus.CHECKED_OUT);

        assertThatThrownBy(() -> bookService.checkoutBook(book, new Member(), 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Book is not available");
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("returnBook resets fields and saves")
    void returnBook_resetsFields() {
        Book book = new Book();
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy("reader@example.com");
        book.setDueDate(LocalDate.now());

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookService.returnBook(book);

        assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(book.getCheckedOutBy()).isNull();
        assertThat(book.getDueDate()).isNull();
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("returnBook throws when already available")
    void returnBook_rejectsAvailable() {
        Book book = new Book();
        book.setStatus(BookStatus.AVAILABLE);

        assertThatThrownBy(() -> bookService.returnBook(book))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Book is not checked out");
        verifyNoInteractions(bookRepository);
    }
}
