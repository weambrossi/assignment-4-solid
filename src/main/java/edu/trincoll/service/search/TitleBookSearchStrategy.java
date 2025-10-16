package edu.trincoll.service.search;

import edu.trincoll.model.Book;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TitleBookSearchStrategy implements BookSearchStrategy {

    private final BookRepository bookRepository;

    public TitleBookSearchStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean supports(String searchType) {
        return "title".equalsIgnoreCase(searchType);
    }

    @Override
    public List<Book> search(String searchTerm) {
        return bookRepository.findByTitleContainingIgnoreCase(searchTerm);
    }
}
