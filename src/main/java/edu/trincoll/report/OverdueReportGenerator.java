package edu.trincoll.report;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.repository.BookRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OverdueReportGenerator implements ReportGenerator {
    private final BookRepository bookRepository;

    public OverdueReportGenerator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public String getReportName() {
        return "overdue";
    }

    @Override
    public String generateReport() {
        LocalDate today = LocalDate.now();
        List<Book> overdueCheckedOut = bookRepository.findByDueDateBefore(today).stream()
                .filter(b -> b.getStatus() == BookStatus.CHECKED_OUT)
                .collect(Collectors.toList());

        if (overdueCheckedOut.isEmpty()) {
            return "No overdue books.";
        }

        return overdueCheckedOut.stream()
                .map(b -> "%s by %s (member %s) — due %s"
                        .formatted(b.getTitle(), b.getAuthor(), b.getCheckedOutBy(), b.getDueDate()))
                .collect(Collectors.joining("\n", "Overdue Books:\n", ""));
    }
}
