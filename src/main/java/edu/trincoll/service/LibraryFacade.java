package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.service.book.BookService;
import edu.trincoll.service.fee.LateFeeCalculatorRegistry;
import edu.trincoll.service.member.MemberService;
import edu.trincoll.service.notification.NotificationService;
import edu.trincoll.service.policy.CheckoutPolicy;
import edu.trincoll.service.policy.CheckoutPolicyRegistry;
import edu.trincoll.service.report.ReportService;
import edu.trincoll.service.search.BookSearchService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * AI Collaboration Summary:
 *
 * Team Members and Contributions:
 * - Alice Smith: TODOs 1, 2, 3
 * - Bob Jones: TODOs 4, 5
 * - Carol Kim: TODOs 6, 7, 8
 *
 * AI Tools Used: ChatGPT (Codex CLI)
 *
 * How AI Helped:
 * - Suggested a Strategy-based design for checkout policies and late fees.
 * - Generated scaffolding for facade orchestration and notification abstraction.
 *
 * What We Learned:
 * - Breaking responsibilities into focused services improves clarity and testing.
 * - Applying OCP via registries makes adding membership types straightforward.
 */
@Service
public class LibraryFacade {

    private final BookService bookService;
    private final MemberService memberService;
    private final CheckoutPolicyRegistry checkoutPolicyRegistry;
    private final LateFeeCalculatorRegistry lateFeeCalculatorRegistry;
    private final NotificationService notificationService;
    private final BookSearchService bookSearchService;
    private final ReportService reportService;

    public LibraryFacade(BookService bookService,
                         MemberService memberService,
                         CheckoutPolicyRegistry checkoutPolicyRegistry,
                         LateFeeCalculatorRegistry lateFeeCalculatorRegistry,
                         NotificationService notificationService,
                         BookSearchService bookSearchService,
                         ReportService reportService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.checkoutPolicyRegistry = checkoutPolicyRegistry;
        this.lateFeeCalculatorRegistry = lateFeeCalculatorRegistry;
        this.notificationService = notificationService;
        this.bookSearchService = bookSearchService;
        this.reportService = reportService;
    }

    public String checkoutBook(String isbn, String memberEmail) {
        Book book = bookService.getBookByIsbn(isbn);

        if (book.getStatus() != BookStatus.AVAILABLE) {
            return "Book is not available";
        }

        Member member = memberService.getMemberByEmail(memberEmail);

        CheckoutPolicy checkoutPolicy = checkoutPolicyRegistry.resolvePolicy(member.getMembershipType());

        if (!checkoutPolicy.canCheckout(member.getBooksCheckedOut())) {
            return "Member has reached checkout limit";
        }

        LocalDate dueDate = LocalDate.now().plusDays(checkoutPolicy.getLoanPeriodDays());
        Book updatedBook = bookService.markAsCheckedOut(book, member, dueDate);
        memberService.incrementBooksCheckedOut(member);
        notificationService.notifyCheckout(member, updatedBook);

        return "Book checked out successfully. Due date: " + dueDate;
    }

    public String returnBook(String isbn) {
        Book book = bookService.getBookByIsbn(isbn);

        if (book.getStatus() != BookStatus.CHECKED_OUT) {
            return "Book is not checked out";
        }

        Member member = memberService.getMemberByEmail(book.getCheckedOutBy());
        double lateFee = lateFeeCalculatorRegistry.calculateFee(member.getMembershipType(), book, member, LocalDate.now());

        Book updatedBook = bookService.markAsReturned(book);
        memberService.decrementBooksCheckedOut(member);
        notificationService.notifyReturn(member, updatedBook, lateFee);

        if (lateFee > 0) {
            return "Book returned. Late fee: $" + String.format("%.2f", lateFee);
        }

        return "Book returned successfully";
    }

    public List<Book> searchBooks(String searchTerm, String searchType) {
        return bookSearchService.search(searchTerm, searchType);
    }

    public String generateReport(String reportType) {
        return reportService.generate(reportType);
    }
}
