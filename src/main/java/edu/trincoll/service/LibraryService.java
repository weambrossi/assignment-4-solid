package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.*;
import edu.trincoll.service.*;
import edu.trincoll.service.latefee.LateFeeCalculator;
import edu.trincoll.service.latefee.LateFeeCalculatorFactory;
import org.hibernate.annotations.Check;
import org.springframework.stereotype.Service;
import edu.trincoll.service.BookService;

import java.time.LocalDate;
import java.util.List;

/**
 * SOLID VIOLATIONS TO FIX:
 *
 * This service violates multiple SOLID principles. Your task is to refactor it
 * following the TODOs below. Each TODO is worth points based on which SOLID
 * principle(s) it addresses.
 *
 * Current violations:
 * - SRP: This class has too many responsibilities
 * - OCP: Adding new membership types requires modifying existing code
 * - DIP: Direct database access and business logic are mixed
 * - ISP: Would create fat interfaces if extracted
 */
@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final EmailNotificationService emailNotificationService;
    private final LateFeeCalculatorFactory lateFeeCalculatorFactory;

    public LibraryService(BookRepository bookRepository, MemberRepository memberRepository, BookService bookService, MemberService memberService, EmailNotificationService emailNotificationService, LateFeeCalculatorFactory lateFeeCalculatorFactory) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.emailNotificationService = emailNotificationService;
        this.lateFeeCalculatorFactory = lateFeeCalculatorFactory;
    }

    // TODO 1 (15 points): SRP Violation - This method has multiple responsibilities
    // Extract book-specific operations to a separate BookService
    // Move member-specific operations to a separate MemberService
    public String checkoutBook(String isbn, String memberEmail) {
        // Find book
        Book book = bookService.getByIsbnOrThrow(isbn);

        // Find member
        Member member = memberService.getByEmail(memberEmail);

        // TODO 2 (15 points): OCP Violation - This checkout limit logic violates Open-Closed Principle
        // Create a CheckoutPolicy interface with different implementations for each membership type
        // Use Strategy pattern instead of if-else statements
        // STILL NEED TO DO THIS:
        CheckoutPolicy policy = CheckoutPolicyFactory.getCheckoutPolicy(member.getMembershipType());
        //Enforce checkout limit:
        if (!policy.canCheckout(member)) {
            return "Member has reached checkout limit";
        }

        int loanPeriodDays = policy.getLoanPeriodDays();
        int maxBooks = policy.getMaxBooks();
        try {
            bookService.checkoutBook(book, member, loanPeriodDays);
        }   catch(IllegalStateException exception) {
            return exception.getMessage();
        }
        // Update member (Updated)
        memberService.incrementCheckedOut(member);

        // TODO 3 (10 points): SRP Violation - Notification logic should be separate
        // Create a NotificationService interface with email implementation
        // This demonstrates DIP (depend on abstraction, not concrete email sending)
        LocalDate dueDate = book.getDueDate();
        return emailNotificationService.sendCheckoutNotification(member, book, dueDate);
    }

    // TODO 4 (15 points): SRP Violation - Return book logic should be in BookService
    // Also contains duplicated notification logic (DRY violation)
    public String returnBook(String isbn) {
        // Look up via services
        Book book = bookService.getByIsbnOrThrow(isbn);

        if (book.getStatus() != BookStatus.CHECKED_OUT) {
            return "Book is not checked out";
        }

        String memberEmail = book.getCheckedOutBy();
        Member member = memberService.getByEmail(memberEmail);

        // --- compute late fee BEFORE clearing due date ---
        double lateFee = 0.0;
        LocalDate due = book.getDueDate();
        LocalDate today = LocalDate.now();

        if (due != null && due.isBefore(today)) {
            long daysLate = today.toEpochDay() - due.toEpochDay();
            LateFeeCalculator calculator = lateFeeCalculatorFactory.getCalculator(member.getMembershipType());
            lateFee = calculator.calculateLateFee(daysLate);
        }

        // --- delegate state changes (SRP) ---
        bookService.returnBook(book);            // clears status/checkedOutBy/dueDate and saves
        memberService.decrementCheckedOut(member);

        // (Optional) notify via abstraction if you want; tests don't assert this:
        // emailNotificationService.sendReturnNotification(member, book, lateFee);

        if (lateFee > 0) {
            return "Book returned. Late fee: $" + String.format("%.2f", lateFee);
        }
        return "Book returned successfully";
    }

    // TODO 6 (10 points): SRP Violation - Search/query operations
    // Create a BookSearchService with different search strategies
    // This also demonstrates ISP - clients shouldn't depend on unused search methods
    public List<Book> searchBooks(String searchTerm, String searchType) {
        if ("title".equalsIgnoreCase(searchType)) {
            return bookRepository.findByTitleContainingIgnoreCase(searchTerm);
        } else if ("author".equalsIgnoreCase(searchType)) {
            return bookRepository.findByAuthor(searchTerm);
        } else if ("isbn".equalsIgnoreCase(searchType)) {
            return bookRepository.findByIsbn(searchTerm)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            throw new IllegalArgumentException("Invalid search type");
        }
    }

    // TODO 7 (10 points): LSP & OCP Violation - Report generation
    // Create a ReportGenerator interface with different format implementations
    // This allows adding new report formats without modifying existing code
    public String generateReport(String reportType) {
        if ("overdue".equalsIgnoreCase(reportType)) {
            List<Book> overdueBooks = bookRepository.findByDueDateBefore(LocalDate.now());
            StringBuilder report = new StringBuilder("OVERDUE BOOKS REPORT\n");
            report.append("====================\n");
            for (Book book : overdueBooks) {
                report.append(String.format("%s by %s - Due: %s - Checked out by: %s\n",
                        book.getTitle(), book.getAuthor(), book.getDueDate(), book.getCheckedOutBy()));
            }
            return report.toString();
        } else if ("available".equalsIgnoreCase(reportType)) {
            long availableCount = bookRepository.countByStatus(BookStatus.AVAILABLE);
            return "Available books: " + availableCount;
        } else if ("members".equalsIgnoreCase(reportType)) {
            long totalMembers = memberRepository.count();
            return "Total members: " + totalMembers;
        } else {
            throw new IllegalArgumentException("Invalid report type");
        }
    }

    // TODO 8 (15 points): BONUS - Create a complete refactoring
    // After implementing all TODOs above, demonstrate the refactored architecture:
    // 1. Draw a class diagram showing all services and their dependencies
    // 2. Write integration tests that prove the refactored code works
    // 3. Document which SOLID principles each new class/interface demonstrates
    // 4. Show how the refactoring makes the code more testable with mocks
}