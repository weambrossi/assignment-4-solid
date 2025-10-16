package edu.trincoll.service.search;

import edu.trincoll.model.Book;

import java.util.List;

public interface BookSearchStrategy {

    boolean supports(String searchType);

    List<Book> search(String searchTerm);
}
