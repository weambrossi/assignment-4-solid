package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.CheckoutPolicy;
import edu.trincoll.repository.CheckoutPolicyFactory;
import edu.trincoll.report.ReportGenerator;
import edu.trincoll.service.latefee.LateFeeCalculator;
import edu.trincoll.service.latefee.LateFeeCalculatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LibraryFacade")
class LibraryFacadeTest {

    @Mock private BookService bookService;
    @Mock private MemberService memberService;
    @Mock private NotificationService notificationService;
    @Mock private LateFeeCalculatorFactory lateFeeCalculatorFactory;
    @Mock private CheckoutPolicyFactory checkoutPolicyFactory;
    @Mock private BookSearchService bookSearchService;

    private ReportGenerator availableReport;
    private ReportGenerator overdueReport;

    private LibraryFacade libraryFacade;

    @BeforeEach
    void setUpReports() {
        availableReport = new SimpleReportGenerator("available", "Available report");
        overdueReport = new SimpleReportGenerator("overdue", "Overdue report");

        libraryFacade = new LibraryFacade(
                bookService,
                memberService,
                notificationService,
                lateFeeCalculatorFactory,
                checkoutPolicyFactory,
                bookSearchService,
                List.of(availableReport, overdueReport)
        );
    }

    @Test
    @DisplayName("checkoutBook returns success message and notifies member")
    void checkoutBook_success() {
        Book book = new Book();
        book.setStatus(BookStatus.AVAILABLE);
        book.setIsbn("123");

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setMembershipType(MembershipType.REGULAR);

        LocalDate today = LocalDate.now();
        CheckoutPolicy policy = mock(CheckoutPolicy.class);

        when(bookService.getByIsbnOrThrow("123")).thenReturn(book);
        when(memberService.getByEmail("user@example.com")).thenReturn(member);
        when(checkoutPolicyFactory.getCheckoutPolicy(MembershipType.REGULAR)).thenReturn(policy);
        when(policy.canCheckout(member)).thenReturn(true);
        when(policy.getLoanPeriodDays()).thenReturn(14);

        doAnswer(invocation -> {
            book.setDueDate(today.plusDays(14));
            book.setStatus(BookStatus.CHECKED_OUT);
            book.setCheckedOutBy(member.getEmail());
            return null;
        }).when(bookService).checkoutBook(book, member, 14);

        String result = libraryFacade.checkoutBook("123", "user@example.com");

        assertThat(result).isEqualTo("Book checked out successfully. Due date: " + today.plusDays(14));
        verify(memberService).incrementCheckedOut(member);
        verify(notificationService).sendCheckoutNotification(member, book, today.plusDays(14));
    }

    @Test
    @DisplayName("checkoutBook stops when policy disallows checkout")
    void checkoutBook_policyBlocksCheckout() {
        Member member = new Member();
        member.setEmail("user@example.com");
        member.setMembershipType(MembershipType.REGULAR);
        Book book = new Book();

        CheckoutPolicy policy = mock(CheckoutPolicy.class);

        when(bookService.getByIsbnOrThrow("123")).thenReturn(book);
        when(memberService.getByEmail("user@example.com")).thenReturn(member);
        when(checkoutPolicyFactory.getCheckoutPolicy(MembershipType.REGULAR)).thenReturn(policy);
        when(policy.canCheckout(member)).thenReturn(false);

        String result = libraryFacade.checkoutBook("123", "user@example.com");

        assertThat(result).isEqualTo("Member has reached checkout limit");
        verify(bookService, never()).checkoutBook(any(), any(), anyInt());
        verify(notificationService, never()).sendCheckoutNotification(any(), any(), any());
    }

    @Test
    @DisplayName("returnBook computes late fee and notifies")
    void returnBook_withLateFee() {
        Book book = new Book();
        book.setIsbn("123");
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy("user@example.com");
        book.setDueDate(LocalDate.now().minusDays(5));

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setMembershipType(MembershipType.REGULAR);

        LateFeeCalculator calculator = daysLate -> daysLate * 0.50;

        when(bookService.getByIsbnOrThrow("123")).thenReturn(book);
        when(memberService.getByEmail("user@example.com")).thenReturn(member);
        when(lateFeeCalculatorFactory.getCalculator(MembershipType.REGULAR)).thenReturn(calculator);

        String result = libraryFacade.returnBook("123");

        assertThat(result).isEqualTo("Book returned. Late fee: $2.50");
        verify(bookService).returnBook(book);
        verify(memberService).decrementCheckedOut(member);
        verify(notificationService).sendReturnNotification(member, book, 2.5);
    }

    @Test
    @DisplayName("returnBook with no late fee still notifies with zero")
    void returnBook_withoutLateFee() {
        Book book = new Book();
        book.setIsbn("123");
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy("user@example.com");
        book.setDueDate(LocalDate.now().plusDays(3));

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setMembershipType(MembershipType.PREMIUM);

        when(bookService.getByIsbnOrThrow("123")).thenReturn(book);
        when(memberService.getByEmail("user@example.com")).thenReturn(member);
        String result = libraryFacade.returnBook("123");

        assertThat(result).isEqualTo("Book returned successfully");
        verify(notificationService).sendReturnNotification(member, book, 0.0);
    }

    @Test
    @DisplayName("returnBook short-circuits when book not checked out")
    void returnBook_whenNotCheckedOut() {
        Book book = new Book();
        book.setStatus(BookStatus.AVAILABLE);

        when(bookService.getByIsbnOrThrow("123")).thenReturn(book);

        String result = libraryFacade.returnBook("123");

        assertThat(result).isEqualTo("Book is not checked out");
        verify(memberService, never()).getByEmail(any());
    }

    @Test
    @DisplayName("searchBooks delegates to BookSearchService")
    void searchBooks_routesByType() {
        Book book = new Book();
        when(bookSearchService.searchByAuthor("Martin")).thenReturn(List.of(book));

        List<Book> result = libraryFacade.searchBooks("Martin", "author");

        assertThat(result).containsExactly(book);
        verify(bookSearchService).searchByAuthor("Martin");
    }

    @Test
    @DisplayName("searchBooks rejects unknown type")
    void searchBooks_invalidType() {
        assertThatThrownBy(() -> libraryFacade.searchBooks("term", "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid search type");
    }

    @Test
    @DisplayName("generateReport routes by report name")
    void generateReport_matchesGenerator() {
        assertThat(libraryFacade.generateReport("available")).isEqualTo("Available report");
        assertThat(libraryFacade.generateReport("OVERDUE")).isEqualTo("Overdue report");
    }

    @Test
    @DisplayName("generateReport rejects invalid report name")
    void generateReport_invalidReport() {
        assertThatThrownBy(() -> libraryFacade.generateReport("members"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid report type");
    }

    private static class SimpleReportGenerator implements ReportGenerator {
        private final String name;
        private final String report;

        private SimpleReportGenerator(String name, String report) {
            this.name = name;
            this.report = report;
        }

        @Override
        public String getReportName() {
            return name;
        }

        @Override
        public String generateReport() {
            return report;
        }
    }
}
