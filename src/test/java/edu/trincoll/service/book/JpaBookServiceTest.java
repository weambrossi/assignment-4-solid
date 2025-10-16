package edu.trincoll.service.book;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaBookService Unit Tests")
class JpaBookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private JpaBookService bookService;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        book = new Book("978-0-11111-11-1", "Test Driven Development", "Kent Beck",
                LocalDate.of(2002, 5, 1));
        member = new Member("Jane Doe", "jane@example.com");
    }

    @Test
    @DisplayName("getBookByIsbn: returns existing book")
    void getBookByIsbnReturnsBook() {
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        Book result = bookService.getBookByIsbn(book.getIsbn());

        assertThat(result).isSameAs(book);
    }

    @Test
    @DisplayName("getBookByIsbn: throws when missing")
    void getBookByIsbnThrowsWhenMissing() {
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookByIsbn(book.getIsbn()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("markAsCheckedOut: updates book status and persists changes")
    void markAsCheckedOutUpdatesBook() {
        LocalDate dueDate = LocalDate.now().plusDays(7);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.markAsCheckedOut(book, member, dueDate);

        assertThat(result.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);
        assertThat(result.getCheckedOutBy()).isEqualTo(member.getEmail());
        assertThat(result.getDueDate()).isEqualTo(dueDate);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(result);
    }

    @Test
    @DisplayName("markAsReturned: clears checkout metadata and persists")
    void markAsReturnedClearsState() {
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy(member.getEmail());
        book.setDueDate(LocalDate.now());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.markAsReturned(book);

        assertThat(result.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(result.getCheckedOutBy()).isNull();
        assertThat(result.getDueDate()).isNull();
    }
}
