package edu.trincoll.service.search;

import edu.trincoll.model.Book;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorBookSearchStrategy implements BookSearchStrategy {

    private final BookRepository bookRepository;

    public AuthorBookSearchStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean supports(String searchType) {
        return "author".equalsIgnoreCase(searchType);
    }

    @Override
    public List<Book> search(String searchTerm) {
        return bookRepository.findByAuthor(searchTerm);
    }
}
