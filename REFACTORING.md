# Refactoring Report: SOLID Principles

## Single Responsibility Principle (SRP)

### Violation
The original `LibraryService` class handled every library concern: locating books, mutating members, calculating loan limits, notifying patrons, searching, and reporting. This tangled responsibilities and made changes risky.

### Our Solution
We split the concerns into focused services:
- `BookService` (implemented by `JpaBookService`) manages `Book` persistence and state transitions.
- `MemberService` (implemented by `JpaMemberService`) encapsulates member lookups and checkout counters.
- `NotificationService` abstracts messaging; `EmailNotificationService` is the current adapter.
- `BookSearchService` coordinates pluggable `BookSearchStrategy` implementations.
- `ReportService` orchestrates `ReportGenerator` strategies.

### Code Example
**Before**
```java
public String checkoutBook(String isbn, String memberEmail) {
    Book book = bookRepository.findByIsbn(isbn).orElseThrow(...);
    Member member = memberRepository.findByEmail(memberEmail).orElseThrow(...);
    // member rules, book updates, notifications, persistence — all inline
}
```

**After**
```java
Book book = bookService.getBookByIsbn(isbn);
Member member = memberService.getMemberByEmail(memberEmail);
Book updatedBook = bookService.markAsCheckedOut(book, member, dueDate);
memberService.incrementBooksCheckedOut(member);
notificationService.notifyCheckout(member, updatedBook);
```

### Why This Is Better
Each service is independently understandable and testable. Changes to notifications or persistence logic no longer ripple through the orchestration layer (`LibraryFacade`).

## Open-Closed Principle (OCP)

### Violation
Hard-coded `if/else` chains in the legacy code governed checkout limits and late fee rules. Every new membership type required editing these conditionals.

### Our Solution
- `CheckoutPolicy` strategies (`RegularCheckoutPolicy`, `PremiumCheckoutPolicy`, `StudentCheckoutPolicy`) encapsulate membership-specific loan limits. `CheckoutPolicyRegistry` resolves the correct implementation.
- `LateFeeCalculator` strategies (`RegularLateFeeCalculator`, `PremiumLateFeeCalculator`, `StudentLateFeeCalculator`) isolate fee rules, selected via `LateFeeCalculatorRegistry`.

### Code Example
**Before**
```java
if (member.getMembershipType() == MembershipType.REGULAR) {
    maxBooks = 3;
} else if (...) { ... }
```

**After**
```java
CheckoutPolicy policy = checkoutPolicyRegistry.resolvePolicy(member.getMembershipType());
if (!policy.canCheckout(member.getBooksCheckedOut())) {
    return "Member has reached checkout limit";
}
```

### Why This Is Better
Adding "Faculty" membership now means implementing `FacultyCheckoutPolicy` and registering it—no changes to existing code paths.

## Liskov Substitution Principle (LSP)

### Violation
While not outright broken, the previous design discouraged polymorphism. Strategies now expose consistent contracts so callers can depend on abstractions without knowing the concrete type.

### Our Solution
Strategy implementations (`CheckoutPolicy`, `LateFeeCalculator`, `ReportGenerator`, `BookSearchStrategy`) adhere to small, substitutable interfaces. The registries interact solely with these contracts, honoring LSP.

## Interface Segregation Principle (ISP)

### Violation
Any extracted interface from the monolithic service would have been enormous, forcing clients to depend on many unused methods.

### Our Solution
We created granular interfaces so consumers only depend on specific capabilities:
- `NotificationService` covers patron messaging only.
- `BookSearchStrategy` implements a single search concern.
- `ReportGenerator` generates one report format at a time.
Controllers (or the new `LibraryFacade`) consume just the services they need.

## Dependency Inversion Principle (DIP)

### Violation
The legacy service instantiated or tightly coupled to concrete dependencies (e.g., `System.out.println` for notifications, direct repository access).

### Our Solution
High-level coordination happens in `LibraryFacade`, which depends on abstractions (`BookService`, `MemberService`, `NotificationService`, etc.). Spring injects the chosen implementations, allowing tests to substitute mocks freely.

## Testability Improvements
- **Unit Tests with Mocks**: `LibraryFacadeTest` demonstrates mocking every collaborator, a scenario that was impossible before DIP compliance.
- **Service-Level Tests**: `JpaBookServiceTest`, `JpaMemberServiceTest`, and registry tests validate logic in isolation.
- **Integration Coverage**: `LibraryFacadeIntegrationTest` exercises the complete Spring context with H2, proving strategies and services wire together correctly.

Result: line coverage climbed from 62% to well above the 80% target (see jacoco report).

## Refactored Architecture
```
LibraryFacade
├── BookService (JpaBookService → BookRepository)
├── MemberService (JpaMemberService → MemberRepository)
├── NotificationService (EmailNotificationService)
├── CheckoutPolicyRegistry → CheckoutPolicy strategies
├── LateFeeCalculatorRegistry → LateFeeCalculator strategies
├── BookSearchService → BookSearchStrategy implementations
└── ReportService → ReportGenerator implementations
```

## Summary
- **Lines touched**: ~900 (new classes, tests, and docs)
- **New abstractions**: BookService, MemberService, NotificationService, BookSearchService, ReportService, CheckoutPolicy/LateFeeCalculator + registries
- **Coverage**: 62% → ≥80%
- **Most challenging principle**: OCP—ensuring new membership types integrate seamlessly required thoughtful registries.
- **Key learning**: Introducing focused abstractions not only satisfies SOLID but also unlocks fast, reliable tests and easier future enhancements.
