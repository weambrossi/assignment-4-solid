package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.report.AvailabilityReportGenerator;
import edu.trincoll.report.MemberCountReportGenerator;
import edu.trincoll.report.OverdueReportGenerator;
import edu.trincoll.repository.BookRepository;
import edu.trincoll.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService (Generators) Tests")
class ReportServiceTest {

    @Nested
    @DisplayName("AvailabilityReportGenerator")
    class AvailabilityReportGeneratorTests {

        @Test
        @DisplayName("returns 'No available books.' when repository is empty")
        void availability_noBooks() {
            BookRepository repo = mock(BookRepository.class);
            when(repo.findByStatus(BookStatus.AVAILABLE)).thenReturn(List.of());

            var gen = new AvailabilityReportGenerator(repo);
            String report = gen.generateReport();

            assertThat(report).isEqualTo("No available books.");
            verify(repo).findByStatus(BookStatus.AVAILABLE);
        }

        @Test
        @DisplayName("lists available books with title/author/isbn")
        void availability_listsBooks() {
            Book b1 = new Book();
            b1.setTitle("Clean Code");
            b1.setAuthor("Robert Martin");
            b1.setIsbn("978-0-123456-78-9");
            b1.setStatus(BookStatus.AVAILABLE);

            Book b2 = new Book();
            b2.setTitle("Refactoring");
            b2.setAuthor("Martin Fowler");
            b2.setIsbn("978-0-987654-32-1");
            b2.setStatus(BookStatus.AVAILABLE);

            BookRepository repo = mock(BookRepository.class);
            when(repo.findByStatus(BookStatus.AVAILABLE)).thenReturn(List.of(b1, b2));

            var gen = new AvailabilityReportGenerator(repo);
            String report = gen.generateReport();

            assertThat(report).startsWith("Available Books:\n");
            assertThat(report).contains("Clean Code by Robert Martin (ISBN 978-0-123456-78-9)");
            assertThat(report).contains("Refactoring by Martin Fowler (ISBN 978-0-987654-32-1)");
            verify(repo).findByStatus(BookStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("OverdueReportGenerator")
    class OverdueReportGeneratorTests {

        @Test
        @DisplayName("returns 'No overdue books.' when none are overdue and checked out")
        void overdue_none() {
            BookRepository repo = mock(BookRepository.class);
            when(repo.findByDueDateBefore(any(LocalDate.class))).thenReturn(List.of());

            var gen = new OverdueReportGenerator(repo);
            String report = gen.generateReport();

            assertThat(report).isEqualTo("No overdue books.");
            verify(repo).findByDueDateBefore(any(LocalDate.class));
        }

        @Test
        @DisplayName("lists only CHECKED_OUT overdue books, ignoring AVAILABLE ones")
        void overdue_listsOnlyCheckedOut() {
            LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);

            Book overdueCheckedOut = new Book();
            overdueCheckedOut.setTitle("Domain-Driven Design");
            overdueCheckedOut.setAuthor("Eric Evans");
            overdueCheckedOut.setIsbn("978-0-321-12521-7");
            overdueCheckedOut.setStatus(BookStatus.CHECKED_OUT);
            overdueCheckedOut.setCheckedOutBy("alice@example.com");
            overdueCheckedOut.setDueDate(fiveDaysAgo);

            Book overdueButAvailable = new Book();
            overdueButAvailable.setTitle("The Pragmatic Programmer");
            overdueButAvailable.setAuthor("Andy Hunt, Dave Thomas");
            overdueButAvailable.setIsbn("978-0-2016-1622-4");
            overdueButAvailable.setStatus(BookStatus.AVAILABLE);
            overdueButAvailable.setDueDate(fiveDaysAgo);

            BookRepository repo = mock(BookRepository.class);
            // The generator filters by status itself after fetching by due date
            when(repo.findByDueDateBefore(any(LocalDate.class)))
                    .thenReturn(List.of(overdueCheckedOut, overdueButAvailable));

            var gen = new OverdueReportGenerator(repo);
            String report = gen.generateReport();

            assertThat(report).startsWith("Overdue Books:\n");
            assertThat(report).contains("Domain-Driven Design by Eric Evans (member alice@example.com) — due " + fiveDaysAgo);
            // Ensure AVAILABLE overdue book is filtered out
            assertThat(report).doesNotContain("The Pragmatic Programmer");

            verify(repo).findByDueDateBefore(any(LocalDate.class));
        }
    }

    @Nested
    @DisplayName("MemberCountReportGenerator")
    class MemberCountReportGeneratorTests {

        @Test
        @DisplayName("reports total member count")
        void memberCount_reportsTotal() {
            MemberRepository repo = mock(MemberRepository.class);
            when(repo.count()).thenReturn(42L);

            var gen = new MemberCountReportGenerator(repo);

            assertThat(gen.getReportName()).isEqualTo("members");
            assertThat(gen.generateReport()).isEqualTo("Total members: 42");
            verify(repo).count();
        }
    }
}
