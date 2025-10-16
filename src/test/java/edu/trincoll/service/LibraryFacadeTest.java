package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.service.book.BookService;
import edu.trincoll.service.fee.LateFeeCalculatorRegistry;
import edu.trincoll.service.member.MemberService;
import edu.trincoll.service.notification.NotificationService;
import edu.trincoll.service.policy.CheckoutPolicy;
import edu.trincoll.service.policy.CheckoutPolicyRegistry;
import edu.trincoll.service.report.ReportService;
import edu.trincoll.service.search.BookSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Library Facade Unit Tests")
class LibraryFacadeTest {

    @Mock
    private BookService bookService;

    @Mock
    private MemberService memberService;

    @Mock
    private CheckoutPolicyRegistry checkoutPolicyRegistry;

    @Mock
    private CheckoutPolicy checkoutPolicy;

    @Mock
    private LateFeeCalculatorRegistry lateFeeCalculatorRegistry;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BookSearchService bookSearchService;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private LibraryFacade libraryFacade;

    private Book availableBook;
    private Member regularMember;
    private Member premiumMember;

    @BeforeEach
    void setUp() {
        availableBook = new Book("978-0-123456-78-9", "Clean Code", "Robert Martin",
                LocalDate.of(2008, 8, 1));
        availableBook.setStatus(BookStatus.AVAILABLE);

        regularMember = new Member("John Doe", "john@example.com", MembershipType.REGULAR);
        regularMember.setBooksCheckedOut(0);

        premiumMember = new Member("Jane Smith", "jane@example.com", MembershipType.PREMIUM);
        premiumMember.setBooksCheckedOut(1);
    }

    @Test
    @DisplayName("checkoutBook: success path delegates to services and notifies member")
    void checkoutBookSuccess() {
        when(bookService.getBookByIsbn(availableBook.getIsbn())).thenReturn(availableBook);
        when(memberService.getMemberByEmail(regularMember.getEmail())).thenReturn(regularMember);
        when(checkoutPolicyRegistry.resolvePolicy(MembershipType.REGULAR)).thenReturn(checkoutPolicy);
        when(checkoutPolicy.getLoanPeriodDays()).thenReturn(14);
        when(checkoutPolicy.canCheckout(regularMember.getBooksCheckedOut())).thenReturn(true);
        when(bookService.markAsCheckedOut(eq(availableBook), eq(regularMember), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate dueDate = invocation.getArgument(2, LocalDate.class);
                    availableBook.setStatus(BookStatus.CHECKED_OUT);
                    availableBook.setCheckedOutBy(regularMember.getEmail());
                    availableBook.setDueDate(dueDate);
                    return availableBook;
                });

        var response = libraryFacade.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        assertThat(response).contains("Book checked out successfully");
        assertThat(availableBook.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);
        verify(memberService).incrementBooksCheckedOut(regularMember);
        verify(notificationService).notifyCheckout(regularMember, availableBook);
    }

    @Test
    @DisplayName("checkoutBook: rejects request when member at limit")
    void checkoutBookRejectsAtLimit() {
        regularMember.setBooksCheckedOut(3);

        when(bookService.getBookByIsbn(availableBook.getIsbn())).thenReturn(availableBook);
        when(memberService.getMemberByEmail(regularMember.getEmail())).thenReturn(regularMember);
        when(checkoutPolicyRegistry.resolvePolicy(MembershipType.REGULAR)).thenReturn(checkoutPolicy);
        when(checkoutPolicy.canCheckout(regularMember.getBooksCheckedOut())).thenReturn(false);

        var response = libraryFacade.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        assertThat(response).isEqualTo("Member has reached checkout limit");
        verify(bookService, never()).markAsCheckedOut(any(), any(), any());
        verify(notificationService, never()).notifyCheckout(any(), any());
    }

    @Test
    @DisplayName("checkoutBook: fails when book unavailable")
    void checkoutBookFailsWhenBookUnavailable() {
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        when(bookService.getBookByIsbn(availableBook.getIsbn())).thenReturn(availableBook);

        var response = libraryFacade.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        assertThat(response).isEqualTo("Book is not available");
        verify(memberService, never()).getMemberByEmail(anyString());
    }

    @Test
    @DisplayName("checkoutBook: bubbles up missing book exceptions")
    void checkoutBookThrowsWhenBookMissing() {
        when(bookService.getBookByIsbn(anyString())).thenThrow(new IllegalArgumentException("Book not found"));

        assertThatThrownBy(() -> libraryFacade.checkoutBook("missing", regularMember.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("returnBook: restores availability and charges late fee when applicable")
    void returnBookProcessesLateFee() {
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        availableBook.setCheckedOutBy(premiumMember.getEmail());
        availableBook.setDueDate(LocalDate.now().minusDays(3));

        when(bookService.getBookByIsbn(availableBook.getIsbn())).thenReturn(availableBook);
        when(memberService.getMemberByEmail(premiumMember.getEmail())).thenReturn(premiumMember);
        when(bookService.markAsReturned(availableBook)).thenAnswer(invocation -> {
            availableBook.setStatus(BookStatus.AVAILABLE);
            availableBook.setCheckedOutBy(null);
            availableBook.setDueDate(null);
            return availableBook;
        });
        when(lateFeeCalculatorRegistry.calculateFee(eq(MembershipType.PREMIUM), eq(availableBook), eq(premiumMember), any(LocalDate.class)))
                .thenReturn(0.0);

        var response = libraryFacade.returnBook(availableBook.getIsbn());

        assertThat(response).isEqualTo("Book returned successfully");
        assertThat(availableBook.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        verify(memberService).decrementBooksCheckedOut(premiumMember);
        verify(notificationService).notifyReturn(premiumMember, availableBook, 0.0);
    }

    @Test
    @DisplayName("returnBook: includes fee in response when calculator returns positive amount")
    void returnBookIncludesLateFeeMessage() {
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        availableBook.setCheckedOutBy(regularMember.getEmail());
        availableBook.setDueDate(LocalDate.now().minusDays(5));

        when(bookService.getBookByIsbn(availableBook.getIsbn())).thenReturn(availableBook);
        when(memberService.getMemberByEmail(regularMember.getEmail())).thenReturn(regularMember);
        when(bookService.markAsReturned(availableBook)).thenReturn(availableBook);
        when(lateFeeCalculatorRegistry.calculateFee(eq(MembershipType.REGULAR), eq(availableBook), eq(regularMember), any(LocalDate.class)))
                .thenReturn(2.50);

        var response = libraryFacade.returnBook(availableBook.getIsbn());

        assertThat(response).isEqualTo("Book returned. Late fee: $2.50");
        verify(notificationService).notifyReturn(regularMember, availableBook, 2.50);
    }

    @Test
    @DisplayName("returnBook: rejects attempts when book not currently checked out")
    void returnBookRejectsWhenAvailable() {
        when(bookService.getBookByIsbn(availableBook.getIsbn())).thenReturn(availableBook);

        var response = libraryFacade.returnBook(availableBook.getIsbn());

        assertThat(response).isEqualTo("Book is not checked out");
        verify(memberService, never()).getMemberByEmail(anyString());
    }

    @Test
    @DisplayName("searchBooks: delegates to search service")
    void searchBooksDelegates() {
        List<Book> expected = List.of(availableBook);
        when(bookSearchService.search("Clean", "title")).thenReturn(expected);

        var results = libraryFacade.searchBooks("Clean", "title");

        assertThat(results).isEqualTo(expected);
        verify(bookSearchService).search("Clean", "title");
    }

    @Test
    @DisplayName("generateReport: delegates to report service")
    void generateReportDelegates() {
        when(reportService.generate("available")).thenReturn("Available books: 42");

        var report = libraryFacade.generateReport("available");

        assertThat(report).isEqualTo("Available books: 42");
        verify(reportService).generate("available");
    }
}
