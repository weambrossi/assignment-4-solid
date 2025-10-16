package edu.trincoll.service.search;

import edu.trincoll.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookSearchService {

    private final List<BookSearchStrategy> strategies;

    public BookSearchService(List<BookSearchStrategy> strategies) {
        this.strategies = strategies;
    }

    public List<Book> search(String searchTerm, String searchType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(searchType))
                .findFirst()
                .map(strategy -> strategy.search(searchTerm))
                .orElseThrow(() -> new IllegalArgumentException("Invalid search type"));
    }
}
