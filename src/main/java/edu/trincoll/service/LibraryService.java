package edu.trincoll.service;

import edu.trincoll.model.Book;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Maintains API compatibility for callers expecting {@code LibraryService}
 * while delegating to the SOLID-compliant {@link LibraryFacade}.
 */
@Service
public class LibraryService {

    private final LibraryFacade libraryFacade;

    public LibraryService(LibraryFacade libraryFacade) {
        this.libraryFacade = libraryFacade;
    }

    public String checkoutBook(String isbn, String memberEmail) {
        return libraryFacade.checkoutBook(isbn, memberEmail);
    }

    public String returnBook(String isbn) {
        return libraryFacade.returnBook(isbn);
    }

    public List<Book> searchBooks(String searchTerm, String searchType) {
        return libraryFacade.searchBooks(searchTerm, searchType);
    }

    public String generateReport(String reportType) {
        return libraryFacade.generateReport(reportType);
    }
}

