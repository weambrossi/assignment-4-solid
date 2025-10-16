package edu.trincoll.service.book;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class JpaBookService implements BookService {

    private final BookRepository bookRepository;

    public JpaBookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Book markAsCheckedOut(Book book, Member member, LocalDate dueDate) {
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy(member.getEmail());
        book.setDueDate(dueDate);
        return save(book);
    }

    @Override
    public Book markAsReturned(Book book) {
        book.setStatus(BookStatus.AVAILABLE);
        book.setCheckedOutBy(null);
        book.setDueDate(null);
        return save(book);
    }

    @Override
    public List<Book> findOverdueBooks(LocalDate when) {
        return bookRepository.findByDueDateBefore(when);
    }

    @Override
    public long countByStatus(BookStatus status) {
        return bookRepository.countByStatus(status);
    }
}
