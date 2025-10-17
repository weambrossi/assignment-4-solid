package edu.trincoll.report;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AvailabilityReportGenerator implements ReportGenerator {
    private final BookRepository bookRepository;

    public AvailabilityReportGenerator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public String getReportName() {
        return "available";
    }

    @Override
    public String generateReport() {
        List<Book> available = bookRepository.findByStatus(BookStatus.AVAILABLE);
        if (available.isEmpty()) return "No available books.";
        return available.stream()
                .map(b -> "%s by %s (ISBN %s)".formatted(b.getTitle(), b.getAuthor(), b.getIsbn()))
                .collect(Collectors.joining("\n", "Available Books:\n", ""));
    }
}

