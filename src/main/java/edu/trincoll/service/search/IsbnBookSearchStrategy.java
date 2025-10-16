package edu.trincoll.service.search;

import edu.trincoll.model.Book;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IsbnBookSearchStrategy implements BookSearchStrategy {

    private final BookRepository bookRepository;

    public IsbnBookSearchStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean supports(String searchType) {
        return "isbn".equalsIgnoreCase(searchType);
    }

    @Override
    public List<Book> search(String searchTerm) {
        return bookRepository.findByIsbn(searchTerm)
                .map(List::of)
                .orElse(List.of());
    }
}
