package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.repository.BookRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BookSearchServiceTest {

    @Test
    void testSearchByTitle() {
        BookRepository repo = mock(BookRepository.class);
        BookSearchService search = new BookSearchService(repo);
        when(repo.findByTitleContainingIgnoreCase("Java")).thenReturn(List.of(new Book()));
        assertEquals(1, search.searchByTitle("Java").size());
    }

    @Test
    void testSearchByAuthor() {
        BookRepository repo = mock(BookRepository.class);
        BookSearchService search = new BookSearchService(repo);
        when(repo.findByAuthor("Robert Martin")).thenReturn(List.of(new Book()));
        assertEquals(1, search.searchByAuthor("Robert Martin").size());
    }

    @Test
    void testSearchByIsbn() {
        BookRepository repo = mock(BookRepository.class);
        BookSearchService search = new BookSearchService(repo);
        Book mockBook = new Book();
        when(repo.findByIsbn("123")).thenReturn(Optional.of(mockBook));
        assertTrue(search.searchByIsbn("123").isPresent());
    }
}
