package edu.trincoll.service;

import edu.trincoll.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("LibraryService facade delegation")
class LibraryServiceTest {

    private final LibraryFacade facade = mock(LibraryFacade.class);
    private final LibraryService service = new LibraryService(facade);

    @Test
    void checkoutDelegates() {
        when(facade.checkoutBook("isbn", "member")).thenReturn("ok");

        String result = service.checkoutBook("isbn", "member");

        assertThat(result).isEqualTo("ok");
        verify(facade).checkoutBook("isbn", "member");
    }

    @Test
    void returnDelegates() {
        when(facade.returnBook("isbn")).thenReturn("returned");

        String result = service.returnBook("isbn");

        assertThat(result).isEqualTo("returned");
        verify(facade).returnBook("isbn");
    }

    @Test
    void searchDelegates() {
        Book book = new Book();
        when(facade.searchBooks("term", "title")).thenReturn(List.of(book));

        List<Book> result = service.searchBooks("term", "title");

        assertThat(result).containsExactly(book);
        verify(facade).searchBooks("term", "title");
    }

    @Test
    void reportDelegates() {
        when(facade.generateReport("available")).thenReturn("report");

        String report = service.generateReport("available");

        assertThat(report).isEqualTo("report");
        verify(facade).generateReport("available");
    }
}

