package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.repository.CheckoutPolicy;
import edu.trincoll.repository.CheckoutPolicyFactory;
import edu.trincoll.report.ReportGenerator;
import edu.trincoll.service.latefee.LateFeeCalculator;
import edu.trincoll.service.latefee.LateFeeCalculatorFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LibraryFacade {

    private final BookService bookService;
    private final MemberService memberService;
    private final NotificationService notificationService;
    private final LateFeeCalculatorFactory lateFeeCalculatorFactory;
    private final CheckoutPolicyFactory checkoutPolicyFactory;
    private final BookSearchService bookSearchService;
    private final Map<String, ReportGenerator> reportGenerators;

    public LibraryFacade(BookService bookService,
                         MemberService memberService,
                         NotificationService notificationService,
                         LateFeeCalculatorFactory lateFeeCalculatorFactory,
                         CheckoutPolicyFactory checkoutPolicyFactory,
                         BookSearchService bookSearchService,
                         List<ReportGenerator> reportGenerators) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.notificationService = notificationService;
        this.lateFeeCalculatorFactory = lateFeeCalculatorFactory;
        this.checkoutPolicyFactory = checkoutPolicyFactory;
        this.bookSearchService = bookSearchService;
        this.reportGenerators = reportGenerators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        gen -> gen.getReportName().toLowerCase(),
                        Function.identity()
                ));
    }

    public String checkoutBook(String isbn, String memberEmail) {
        Book book = bookService.getByIsbnOrThrow(isbn);
        Member member = memberService.getByEmail(memberEmail);

        CheckoutPolicy policy = checkoutPolicyFactory.getCheckoutPolicy(member.getMembershipType());
        if (!policy.canCheckout(member)) {
            return "Member has reached checkout limit";
        }

        try {
            bookService.checkoutBook(book, member, policy.getLoanPeriodDays());
        } catch (IllegalStateException ex) {
            return ex.getMessage();
        }

        memberService.incrementCheckedOut(member);

        LocalDate dueDate = book.getDueDate();
        notificationService.sendCheckoutNotification(member, book, dueDate);

        return "Book checked out successfully. Due date: " + dueDate;
    }

    public String returnBook(String isbn) {
        Book book = bookService.getByIsbnOrThrow(isbn);
        if (book.getStatus() != BookStatus.CHECKED_OUT) {
            return "Book is not checked out";
        }

        Member member = memberService.getByEmail(book.getCheckedOutBy());

        double lateFee = calculateLateFee(book, member);

        bookService.returnBook(book);
        memberService.decrementCheckedOut(member);

        notificationService.sendReturnNotification(member, book, lateFee);

        if (lateFee > 0) {
            return "Book returned. Late fee: $" + String.format("%.2f", lateFee);
        }
        return "Book returned successfully";
    }

    private double calculateLateFee(Book book, Member member) {
        LocalDate due = book.getDueDate();
        LocalDate today = LocalDate.now();
        if (due == null || !due.isBefore(today)) {
            return 0.0;
        }

        long daysLate = ChronoUnit.DAYS.between(due, today);
        LateFeeCalculator calculator = lateFeeCalculatorFactory.getCalculator(member.getMembershipType());
        return calculator.calculateLateFee(daysLate);
    }

    public List<Book> searchBooks(String searchTerm, String searchType) {
        switch (searchType.toLowerCase()) {
            case "title":
                return bookSearchService.searchByTitle(searchTerm);
            case "author":
                return bookSearchService.searchByAuthor(searchTerm);
            case "isbn":
                return bookSearchService.searchByIsbn(searchTerm)
                        .map(List::of)
                        .orElse(List.of());
            default:
                throw new IllegalArgumentException("Invalid search type");
        }
    }

    public String generateReport(String reportType) {
        ReportGenerator generator = reportGenerators.get(reportType.toLowerCase());
        if (generator == null) {
            throw new IllegalArgumentException("Invalid report type");
        }
        return generator.generateReport();
    }
}

