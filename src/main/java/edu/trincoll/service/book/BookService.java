package edu.trincoll.service.book;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;

import java.time.LocalDate;
import java.util.List;

/**
 * Abstraction over book-related persistence operations so that library workflows
 * do not need to manipulate repositories directly.
 */
public interface BookService {

    Book getBookByIsbn(String isbn);

    Book save(Book book);

    default boolean isAvailable(Book book) {
        return book.getStatus() == BookStatus.AVAILABLE;
    }

    Book markAsCheckedOut(Book book, Member member, LocalDate dueDate);

    Book markAsReturned(Book book);

    List<Book> findOverdueBooks(LocalDate when);

    long countByStatus(BookStatus status);
}
