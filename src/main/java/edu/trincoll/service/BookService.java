package edu.trincoll.service;

import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Service;
import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;

import java.time.LocalDate;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public boolean isAvailable(Book book) {
        return book.getStatus() == BookStatus.AVAILABLE;
    }

    public Book getByIsbnOrThrow(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    public void checkoutBook(Book book, Member member, int loanPeriodDays) {
        if (!isAvailable(book)) {
            throw new IllegalStateException("Book is not available");
        }
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy(member.getEmail());
        book.setDueDate(LocalDate.now().plusDays(loanPeriodDays));

        bookRepository.save(book);
    }

    public void returnBook(Book book) {
        if (book.getStatus() != BookStatus.CHECKED_OUT) {
            throw new IllegalStateException("Book is not checked out");
        }
        book.setStatus(BookStatus.AVAILABLE);
        book.setCheckedOutBy(null);
        book.setDueDate(null);
        bookRepository.save(book);
    }
}
