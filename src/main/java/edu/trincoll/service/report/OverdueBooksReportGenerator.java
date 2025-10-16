package edu.trincoll.service.report;

import edu.trincoll.model.Book;
import edu.trincoll.service.book.BookService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class OverdueBooksReportGenerator implements ReportGenerator {

    private final BookService bookService;

    public OverdueBooksReportGenerator(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public boolean supports(String reportType) {
        return "overdue".equalsIgnoreCase(reportType);
    }

    @Override
    public String generate() {
        List<Book> overdueBooks = bookService.findOverdueBooks(LocalDate.now());
        StringBuilder report = new StringBuilder("OVERDUE BOOKS REPORT\n");
        report.append("====================\n");
        overdueBooks.forEach(book -> report.append(String.format(
                "%s by %s - Due: %s - Checked out by: %s\n",
                book.getTitle(), book.getAuthor(), book.getDueDate(), book.getCheckedOutBy())));
        return report.toString();
    }
}
