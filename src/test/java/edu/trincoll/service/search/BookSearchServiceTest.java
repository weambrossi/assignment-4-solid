package edu.trincoll.service.search;

import edu.trincoll.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BookSearchService Tests")
class BookSearchServiceTest {

    private final BookSearchStrategy titleStrategy = new BookSearchStrategy() {
        @Override
        public boolean supports(String searchType) {
            return "title".equalsIgnoreCase(searchType);
        }

        @Override
        public List<Book> search(String searchTerm) {
            return List.of();
        }
    };

    private final BookSearchService service = new BookSearchService(List.of(titleStrategy));

    @Test
    @DisplayName("search: delegates to matching strategy")
    void searchDelegatesToStrategy() {
        List<Book> result = service.search("Clean Code", "title");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("search: throws for unsupported type")
    void searchThrowsForUnsupportedType() {
        assertThatThrownBy(() -> service.search("anything", "isbn"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid search type");
    }
}
