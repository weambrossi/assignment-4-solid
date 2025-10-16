package edu.trincoll.service.report;

import edu.trincoll.model.BookStatus;
import edu.trincoll.service.book.BookService;
import org.springframework.stereotype.Component;

@Component
public class AvailableBooksReportGenerator implements ReportGenerator {

    private final BookService bookService;

    public AvailableBooksReportGenerator(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public boolean supports(String reportType) {
        return "available".equalsIgnoreCase(reportType);
    }

    @Override
    public String generate() {
        long availableCount = bookService.countByStatus(BookStatus.AVAILABLE);
        return "Available books: " + availableCount;
    }
}
