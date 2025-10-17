# Refactoring Report: SOLID Principles

## Single Responsibility Principle (SRP)

### Violation
The original `LibraryService` class owned every concern:
- Book lifecycle (availability checks, checkout, return)
- Member persistence and checkout counters
- Notifications via `System.out.println`
- Search queries against the repositories
- Reporting logic with string assembly

Keeping all of that in one class produced tangled conditionals, hard-to-test flows, and poor cohesion.

### Our Solution
We extracted focused collaborators, each with one reason to change:
- `BookService` handles only book state transitions (`src/main/java/edu/trincoll/service/BookService.java`)
- `MemberService` updates member records and counters (`src/main/java/edu/trincoll/service/MemberService.java`)
- `NotificationService`/`EmailNotificationService` encapsulate messaging (`src/main/java/edu/trincoll/service/NotificationService.java`)
- `BookSearchService` owns catalogue queries (`src/main/java/edu/trincoll/service/BookSearchService.java`)
- `ReportGenerator` implementations create individual reports (`src/main/java/edu/trincoll/report/*.java`)
- `LibraryFacade` orchestrates the workflow without performing low-level work (`src/main/java/edu/trincoll/service/LibraryFacade.java`)

### Code Example
**Before (collapsed responsibilities):**
```java
public class LibraryService {
    public String checkoutBook(String isbn, String memberEmail) {
        Book book = bookRepository.findByIsbn(isbn).orElseThrow();
        if (book.getStatus() != BookStatus.AVAILABLE) {
            return "Book is not available";
        }
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow();
        member.setBooksCheckedOut(member.getBooksCheckedOut() + 1);
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy(memberEmail);
        book.setDueDate(LocalDate.now().plusDays(14));
        bookRepository.save(book);
        memberRepository.save(member);
        System.out.println("Book checked out: " + book.getTitle());
        return "Success";
    }
}
```

**After (single responsibility services orchestrated by the facade):**
```java
public String checkoutBook(String isbn, String memberEmail) {
    Book book = bookService.getByIsbnOrThrow(isbn);
    Member member = memberService.getByEmail(memberEmail);
    CheckoutPolicy policy = checkoutPolicyFactory.getCheckoutPolicy(member.getMembershipType());
    if (!policy.canCheckout(member)) {
        return "Member has reached checkout limit";
    }
    bookService.checkoutBook(book, member, policy.getLoanPeriodDays());
    memberService.incrementCheckedOut(member);
    notificationService.sendCheckoutNotification(member, book, book.getDueDate());
    return "Book checked out successfully. Due date: " + book.getDueDate();
} // src/main/java/edu/trincoll/service/LibraryFacade.java:44
```

### Why This Is Better
- Each unit (book, member, notification, reporting) can be evolved or tested independently.
- Changes to checkout rules no longer risk the reporting or notification code paths.
- Controllers consume a simple `LibraryService` facade without knowing underlying details.
- The refactoring shrinks the surface area for defects and clarifies ownership of each behaviour.

---

## Open-Closed Principle (OCP)

### Violation
Checkout logic hard-coded membership comparisons and loan limits:
```java
if (member.getMembershipType() == MembershipType.REGULAR) {
    maxBooks = 3;
    loanPeriod = 14;
} else if (member.getMembershipType() == MembershipType.PREMIUM) {
    maxBooks = 10;
    loanPeriod = 30;
} // … add more else-if blocks for every new tier
```
Every new membership required editing this method, risking regressions.

### Our Solution
We introduced the `CheckoutPolicy` strategy and a componentised `CheckoutPolicyFactory` that Spring populates with available policies (`src/main/java/edu/trincoll/repository/CheckoutPolicyFactory.java`). Each membership type supplies its own policy class (`RegularCheckoutPolicy`, `PremiumCheckoutPolicy`, `StudentCheckoutPolicy`).

### Code Example
**After:**
```java
CheckoutPolicy policy = checkoutPolicyFactory.getCheckoutPolicy(member.getMembershipType());
if (!policy.canCheckout(member)) {
    return "Member has reached checkout limit";
}
bookService.checkoutBook(book, member, policy.getLoanPeriodDays());
```
`src/main/java/edu/trincoll/service/LibraryFacade.java:47`

### Why This Is Better
- Adding a membership tier is a matter of creating a new `CheckoutPolicy` bean; the facade remains closed for modification.
- Policies encapsulate their own invariants, yielding clearer business code and targeted unit tests (`src/test/java/edu/trincoll/repository/CheckoutPolicyFactoryTest.java`).
- Configuration is declarative—Spring assembles the map of policies without manual updates.

---

## Liskov Substitution Principle (LSP)

### Violation
Report generation used `if/else` chains that built different reports inline. Swapping or adding reports required touching the central method, and there was no shared contract to guarantee behaviour across report types.

### Our Solution
We defined the `ReportGenerator` abstraction, including a `getReportName()` discriminator, and created concrete generators (`AvailabilityReportGenerator`, `OverdueReportGenerator`, `MemberCountReportGenerator`). The facade resolves the proper implementation from a map constructed at runtime (`src/main/java/edu/trincoll/service/LibraryFacade.java:93`).

### Code Example
```java
public interface ReportGenerator {
    String getReportName();
    String generateReport();
}
```
```java
ReportGenerator generator = reportGenerators.get(reportType.toLowerCase());
if (generator == null) {
    throw new IllegalArgumentException("Invalid report type");
}
return generator.generateReport();
```
`src/main/java/edu/trincoll/service/LibraryFacade.java:94`

### Why This Is Better
- Any class honouring `ReportGenerator` can replace another without breaking callers (true substitutability).
- New formats plug in without modifying existing logic, protecting closed behaviour.
- Tests cover each generator independently (`src/test/java/edu/trincoll/service/ReportServiceTest.java`).

---

## Interface Segregation Principle (ISP)

### Violation
Clients depending on the monolithic `LibraryService` were forced to rely on methods they did not use (search, reporting, notifications, etc.), making future interface extraction impractical.

### Our Solution
We introduced narrow interfaces/services:
- `BookSearchService` advertises only search methods (`src/main/java/edu/trincoll/service/BookSearchService.java`)
- Report generators expose just `generateReport`
- The facade presents a trimmed API that controllers can consume (`checkoutBook`, `returnBook`, `searchBooks`, `generateReport`).

### Code Example
```java
public class BookSearchService {
    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }
    public Optional<Book> searchByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
}
```

### Why This Is Better
- Callers now depend on the minimal surface they actually use (e.g., tests target `BookSearchService` directly in `src/test/java/edu/trincoll/service/BookSearchServiceTest.java`).
- A change to search logic has zero impact on checkout or reporting consumers.
- It is straightforward to swap implementations (e.g., remote search) without disturbing the rest of the system.

---

## Dependency Inversion Principle (DIP)

### Violation
High-level policies printed directly to the console, binding business logic to low-level IO:
```java
System.out.println("Book checked out: " + book.getTitle());
```
No abstraction existed between the checkout workflow and notification mechanism.

### Our Solution
We inverted the dependency by introducing `NotificationService` and injecting it into the facade (`src/main/java/edu/trincoll/service/LibraryFacade.java:24`). `EmailNotificationService` is one implementation; tests supply mocks to assert behaviour (`src/test/java/edu/trincoll/service/LibraryFacadeTest.java`).

### Code Example
```java
public interface NotificationService {
    void sendCheckoutNotification(Member member, Book book, LocalDate dueDate);
    void sendReturnNotification(Member member, Book book, double lateFee);
}
```
```java
notificationService.sendCheckoutNotification(member, book, dueDate);
```

### Why This Is Better
- The facade depends on the stable abstraction rather than a concrete console implementation.
- Alternative channels (SMS, push) can be introduced without altering the orchestration logic.
- Unit tests replace the dependency with mocks, demonstrating improved testability (see `LibraryFacadeTest.checkoutBook_success`).

---

## Integration & Facade (TODO 8)

### LibraryFacade
`LibraryFacade` centralises orchestration, coordinating the specialised services, enforcing policies, and applying strategies (`src/main/java/edu/trincoll/service/LibraryFacade.java`). A thin `LibraryService` wrapper preserves the existing controller API while delegating to the facade (`src/main/java/edu/trincoll/service/LibraryService.java`).

### Testing Improvements
- `LibraryFacadeTest` isolates the facade with mocks for every collaborator, proving the refactoring makes the workflow highly testable (`src/test/java/edu/trincoll/service/LibraryFacadeTest.java`).
- `LibraryServiceTest` verifies the facade pattern surface is preserved for controllers.
- Existing service tests (search, reports, late fees) still pass, showing backwards compatibility.
- `CheckoutPolicyFactoryTest` demonstrates the new extensible policy wiring.

The complete suite now covers 28 tests, all passing (`./gradlew test`).

