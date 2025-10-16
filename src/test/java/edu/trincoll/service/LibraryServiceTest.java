package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.BookRepository;
import edu.trincoll.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Library Service Tests")
class LibraryServiceTest {

    // Mocked repos
    @Mock private BookRepository bookRepository;
    @Mock private MemberRepository memberRepository;

    // Real services with mocked repos injected
    @InjectMocks private BookService bookService;
    @InjectMocks private MemberService memberService;

    // Mocked notification (LibraryService returns its message)
    @Mock private EmailNotificationService emailNotificationService;

    // Class under test
    @InjectMocks private LibraryService libraryService;

    private Book availableBook;
    private Member regularMember;
    private Member premiumMember;
    private Member studentMember;

    @BeforeEach
    void setUp() throws Exception {
        availableBook = new Book("978-0-123456-78-9", "Clean Code", "Robert Martin",
                LocalDate.of(2008, 8, 1));
        availableBook.setId(1L);
        availableBook.setStatus(BookStatus.AVAILABLE);

        regularMember = new Member("John Doe", "john@example.com");
        regularMember.setId(1L);
        regularMember.setMembershipType(MembershipType.REGULAR);
        regularMember.setBooksCheckedOut(0);

        premiumMember = new Member("Jane Smith", "jane@example.com");
        premiumMember.setId(2L);
        premiumMember.setMembershipType(MembershipType.PREMIUM);
        premiumMember.setBooksCheckedOut(0);

        studentMember = new Member("Bob Student", "bob@example.com");
        studentMember.setId(3L);
        studentMember.setMembershipType(MembershipType.STUDENT);
        studentMember.setBooksCheckedOut(0);

        // ---- CRITICAL: manually inject services into LibraryService (since your prod constructor doesn't) ----
        inject(libraryService, "bookService", bookService);
        inject(libraryService, "memberService", memberService);
        inject(libraryService, "emailNotificationService", emailNotificationService);
    }

    // Simple reflection injector for private fields
    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("Should checkout book successfully for regular member")
    void shouldCheckoutBookForRegularMember() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(regularMember.getEmail()))
                .thenReturn(Optional.of(regularMember));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailNotificationService.sendCheckoutNotification(eq(regularMember), eq(availableBook), any(LocalDate.class)))
                .thenAnswer(inv -> "Book checked out successfully. Due date: " + inv.getArgument(2));

        // Act
        String result = libraryService.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).contains("Book checked out successfully");
        assertThat(result).contains("Due date:");
        verify(bookRepository).save(argThat(book ->
                book.getStatus() == BookStatus.CHECKED_OUT &&
                        regularMember.getEmail().equals(book.getCheckedOutBy()) &&
                        book.getDueDate().equals(today.plusDays(14))
        ));
        verify(memberRepository).save(argThat(member ->
                member.getBooksCheckedOut() == 1
        ));
    }

    @Test
    @DisplayName("Should apply correct loan period for premium member")
    void shouldApplyPremiumLoanPeriod() {
        // Arrange
        LocalDate today = LocalDate.now();
        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(premiumMember.getEmail()))
                .thenReturn(Optional.of(premiumMember));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailNotificationService.sendCheckoutNotification(eq(premiumMember), eq(availableBook), any(LocalDate.class)))
                .thenAnswer(inv -> "Book checked out successfully. Due date: " + inv.getArgument(2));

        // Act
        libraryService.checkoutBook(availableBook.getIsbn(), premiumMember.getEmail());

        // Assert
        verify(bookRepository).save(argThat(book ->
                book.getDueDate().equals(today.plusDays(30))
        ));
    }

    @Test
    @DisplayName("Should enforce checkout limit for regular member")
    void shouldEnforceCheckoutLimitForRegularMember() {
        // Arrange
        regularMember.setBooksCheckedOut(3); // At limit
        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(regularMember.getEmail()))
                .thenReturn(Optional.of(regularMember));

        // Act
        String result = libraryService.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).isEqualTo("Member has reached checkout limit");
        verify(bookRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
        verifyNoInteractions(emailNotificationService);
    }

    @Test
    @DisplayName("Should not checkout unavailable book")
    void shouldNotCheckoutUnavailableBook() {
        // Arrange
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(regularMember.getEmail()))
                .thenReturn(Optional.of(regularMember));

        // Act
        String result = libraryService.checkoutBook(availableBook.getIsbn(), regularMember.getEmail());

        // Assert
        assertThat(result).isEqualTo("Book is not available");
        verify(bookRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
        verifyNoInteractions(emailNotificationService);
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void shouldThrowExceptionWhenBookNotFound() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                libraryService.checkoutBook("invalid-isbn", regularMember.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book not found");
        verifyNoInteractions(emailNotificationService);
    }

    @Test
    @DisplayName("Should return book successfully")
    void shouldReturnBookSuccessfully() {
        // Arrange
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        availableBook.setCheckedOutBy(regularMember.getEmail());
        availableBook.setDueDate(LocalDate.now().plusDays(7));

        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(regularMember.getEmail()))
                .thenReturn(Optional.of(regularMember));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        regularMember.setBooksCheckedOut(1);

        // Act
        String result = libraryService.returnBook(availableBook.getIsbn());

        // Assert
        assertThat(result).isEqualTo("Book returned successfully");
        verify(bookRepository).save(argThat(book ->
                book.getStatus() == BookStatus.AVAILABLE &&
                        book.getCheckedOutBy() == null &&
                        book.getDueDate() == null
        ));
        verify(memberRepository).save(argThat(member ->
                member.getBooksCheckedOut() == 0
        ));
        verifyNoInteractions(emailNotificationService); // your return path prints to stdout only
    }

    @Test
    @DisplayName("Should calculate late fee for regular member")
    void shouldCalculateLateFeeForRegularMember() {
        // Arrange
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        availableBook.setCheckedOutBy(regularMember.getEmail());
        availableBook.setDueDate(LocalDate.now().minusDays(5)); // 5 days late

        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(regularMember.getEmail()))
                .thenReturn(Optional.of(regularMember));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        regularMember.setBooksCheckedOut(1);

        // Act
        String result = libraryService.returnBook(availableBook.getIsbn());

        // Assert
        assertThat(result).contains("Late fee: $2.50"); // 5 days * $0.50
        verifyNoInteractions(emailNotificationService);
    }

    @Test
    @DisplayName("Should not charge late fee for premium member")
    void shouldNotChargeLateFeeForPremiumMember() {
        // Arrange
        availableBook.setStatus(BookStatus.CHECKED_OUT);
        availableBook.setCheckedOutBy(premiumMember.getEmail());
        availableBook.setDueDate(LocalDate.now().minusDays(5)); // 5 days late

        when(bookRepository.findByIsbn(availableBook.getIsbn()))
                .thenReturn(Optional.of(availableBook));
        when(memberRepository.findByEmail(premiumMember.getEmail()))
                .thenReturn(Optional.of(premiumMember));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        premiumMember.setBooksCheckedOut(1);

        // Act
        String result = libraryService.returnBook(availableBook.getIsbn());

        // Assert
        assertThat(result).isEqualTo("Book returned successfully");
        assertThat(result).doesNotContain("Late fee");
        verifyNoInteractions(emailNotificationService);
    }

    @Test
    @DisplayName("Should search books by title")
    void shouldSearchBooksByTitle() {
        // Arrange
        when(bookRepository.findByTitleContainingIgnoreCase("Clean"))
                .thenReturn(java.util.List.of(availableBook));

        // Act
        var results = libraryService.searchBooks("Clean", "title");

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Should throw exception for invalid search type")
    void shouldThrowExceptionForInvalidSearchType() {
        // Act & Assert
        assertThatThrownBy(() ->
                libraryService.searchBooks("test", "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid search type");
    }
}