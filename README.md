# Assignment 4: SOLID Principles with Spring Data JPA

**Due:** Thursday, October 23 at 11:59 PM
**Points:** 100
**Submission:** Via GitHub (one per team)

## Overview

This assignment combines two critical concepts from recent weeks: **SOLID design principles** and **Spring Data JPA**. You'll refactor a library management system that currently violates multiple SOLID principles while working with a real database using JPA.

## Learning Objectives

- Apply all five SOLID principles in practice
- Refactor code from violations to clean design
- Use Spring Data JPA for persistence
- Implement Strategy and other design patterns
- Write tests that prove your refactored code works
- Document design decisions and trade-offs

## What You're Given

A working but poorly designed Library Management System with:
- ✅ **Domain Models**: `Book` and `Member` entities with JPA annotations
- ✅ **Repositories**: Spring Data JPA repositories (already following SOLID!)
- ❌ **Service Layer**: `LibraryService` with **intentional SOLID violations**
- ✅ **Tests**: Comprehensive test suite for the existing (bad) design

Your task: **Refactor the service layer to follow SOLID principles**.

## Current SOLID Violations

The `LibraryService` class violates multiple SOLID principles:

### 1. Single Responsibility Principle (SRP)
- One class handles: book operations, member operations, notifications, search, reporting
- Each of these should be a separate service

### 2. Open-Closed Principle (OCP)
- Checkout limits use if-else for membership types
- Late fee calculation uses if-else for membership types
- Adding new membership types requires modifying existing code

### 3. Liskov Substitution Principle (LSP)
- Not directly violated, but the design doesn't allow for proper inheritance

### 4. Interface Segregation Principle (ISP)
- If we extracted interfaces from current design, they would be "fat" interfaces
- Clients would depend on methods they don't use

### 5. Dependency Inversion Principle (DIP)
- Notification logic is hard-coded (System.out.println)
- No abstraction between high-level logic and low-level details

## Team Collaboration Requirements

This is a **team assignment** with one submission per team. All team members must contribute meaningfully to the codebase.

### Team Setup
1. **One team member** forks the assignment repository
2. **Fork owner** adds teammates as collaborators:
   - Go to fork Settings → Collaborators → Add people
3. **All team members** clone the shared fork and commit their work

### Work Distribution
- Each team member must complete **at least one TODO**
- Distribute the remaining TODOs however your team prefers
- Consider your team's strengths when assigning work

**Example for 3-person team:**
- Alice: TODOs 1, 2, 3 (Service extraction + notifications)
- Bob: TODOs 4, 5 (Member service + late fees)
- Carol: TODOs 6, 7, 8 (Search, reports, integration)

### Git Best Practices (Encouraged but not required)
- Use feature branches for your work: `git checkout -b feature/book-service`
- Write descriptive commit messages
- Pull before you push to avoid conflicts
- Consider creating pull requests for practice (optional)

### Collaboration Grading (10 points)
**I will review your repository's git history to verify all team members contributed.**

Check your contributions: `GitHub → Insights → Contributors`

Teams where only one person made commits will lose collaboration points.

## Your Task: 8 TODOs (100 points)

Each TODO in `LibraryService.java` explains what to refactor and why. Here's the breakdown:

### TODO 1: Extract BookService (15 points)
**SRP Violation Fix**

Extract book-specific operations into `BookService`:
- Book checkout logic
- Book return logic
- Book availability checking

```java
@Service
public class BookService {
    private final BookRepository bookRepository;

    public void checkoutBook(Book book, Member member, int loanPeriodDays) {
        // Just book operations
    }

    public void returnBook(Book book) {
        // Just book operations
    }
}
```

### TODO 2: Create CheckoutPolicy Strategy (15 points)
**OCP Violation Fix**

Replace if-else statements with Strategy pattern:

```java
public interface CheckoutPolicy {
    int getMaxBooks();
    int getLoanPeriodDays();
    boolean canCheckout(Member member);
}

public class RegularCheckoutPolicy implements CheckoutPolicy {
    public int getMaxBooks() { return 3; }
    public int getLoanPeriodDays() { return 14; }
    // ...
}

public class PremiumCheckoutPolicy implements CheckoutPolicy {
    public int getMaxBooks() { return 10; }
    public int getLoanPeriodDays() { return 30; }
    // ...
}
```

Use a factory or configuration to select the right policy:
```java
@Component
public class CheckoutPolicyFactory {
    public CheckoutPolicy getPolicyFor(MembershipType type) {
        return switch (type) {
            case REGULAR -> new RegularCheckoutPolicy();
            case PREMIUM -> new PremiumCheckoutPolicy();
            case STUDENT -> new StudentCheckoutPolicy();
        };
    }
}
```

### TODO 3: Create NotificationService (10 points)
**SRP & DIP Violation Fix**

Extract notification logic and depend on abstraction:

```java
public interface NotificationService {
    void sendCheckoutNotification(Member member, Book book, LocalDate dueDate);
    void sendReturnNotification(Member member, Book book, double lateFee);
}

@Service
public class EmailNotificationService implements NotificationService {
    // Actual email sending implementation
    // For now, can still use System.out.println
}
```

Now services depend on the `NotificationService` interface (DIP), not concrete implementation.

### TODO 4: Extract MemberService (15 points)
**SRP Violation Fix**

Create `MemberService` for member-specific operations:
- Update checkout count
- Retrieve member details
- Member validation

```java
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public void incrementCheckoutCount(Member member) {
        member.setBooksCheckedOut(member.getBooksCheckedOut() + 1);
        memberRepository.save(member);
    }

    public void decrementCheckoutCount(Member member) {
        member.setBooksCheckedOut(member.getBooksCheckedOut() - 1);
        memberRepository.save(member);
    }
}
```

### TODO 5: Create LateFeeCalculator Strategy (10 points)
**OCP Violation Fix**

Extract late fee calculation with Strategy pattern:

```java
public interface LateFeeCalculator {
    double calculateLateFee(long daysLate);
}

public class RegularLateFeeCalculator implements LateFeeCalculator {
    public double calculateLateFee(long daysLate) {
        return daysLate * 0.50;
    }
}

public class PremiumLateFeeCalculator implements LateFeeCalculator {
    public double calculateLateFee(long daysLate) {
        return 0.0; // No late fees for premium
    }
}
```

### TODO 6: Create BookSearchService (10 points)
**SRP & ISP Violation Fix**

Extract search operations:

```java
@Service
public class BookSearchService {
    private final BookRepository bookRepository;

    public List<Book> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> searchByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    public Optional<Book> searchByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
}
```

Clients can now depend only on the search methods they need (ISP).

### TODO 7: Create ReportGenerator Strategy (10 points)
**OCP & LSP Violation Fix**

Use Strategy pattern for reports:

```java
public interface ReportGenerator {
    String generateReport();
}

public class OverdueReportGenerator implements ReportGenerator {
    private final BookRepository bookRepository;

    public String generateReport() {
        // Generate overdue books report
    }
}

public class AvailabilityReportGenerator implements ReportGenerator {
    private final BookRepository bookRepository;

    public String generateReport() {
        // Generate available books report
    }
}
```

### TODO 8: Integration & Documentation (5 points)

After completing all refactoring:

1. **Create LibraryFacade**
   - Coordinate between all services
   - Present simplified API to controllers
   - Demonstrates Facade pattern

2. **Update Tests**
   - Ensure all existing tests still pass
   - Add tests for new services
   - Demonstrate testability improvements with mocks

## Architecture After Refactoring

```
┌─────────────────────────────────────────┐
│         LibraryFacade                   │  ← Simplified API
│  (orchestrates all services)            │
└─────────────────────────────────────────┘
                 ↓ depends on
    ┌────────────┴────────────┐
    ↓                         ↓
┌──────────┐            ┌──────────────┐
│ BookServ │            │ MemberService│
│  ice     │            │              │
└──────────┘            └──────────────┘
    ↓                         ↓
    ↓                   ┌──────────────┐
    ↓                   │Notification  │  ← Interface (DIP)
    ↓                   │Service       │
    ↓                   └──────────────┘
    ↓                         ↑
┌──────────────┐         implements
│CheckoutPolicy│  ← Strategy (OCP)
│  Factory     │
└──────────────┘
    ↓
┌──────────────┐
│RegularPolicy │
│PremiumPolicy │  ← Different implementations
│StudentPolicy │
└──────────────┘
```

## Testing Requirements

### Coverage Goal: 80%+
**Current baseline:** 62% coverage (service layer only)
**Your goal:** 80%+ coverage after refactoring

As you extract services and add tests for them, your coverage will naturally improve.

```bash
./gradlew test
./gradlew jacocoTestReport
# Check build/reports/jacoco/test/html/index.html
```

**Note:** Coverage excludes JPA entity classes and the Application class. Focus on testing business logic in your service classes.

### Test Categories

1. **Unit Tests** for each new service
   - Mock dependencies
   - Test in isolation
   - Fast execution

2. **Integration Tests** for LibraryFacade
   - Test service coordination
   - Use actual Spring context
   - Test with H2 database

3. **Demonstrate Testability**
   - Show how Strategy pattern enables easy testing
   - Show how DIP makes mocking easier
   - Compare to testing the original monolithic service

## Spring Data JPA Usage

This assignment uses **Spring Data JPA** with an **H2 in-memory database**:

### Entities
- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- Relationships and constraints
- JPA lifecycle callbacks

### Repositories
- Extend `JpaRepository<T, ID>`
- Query methods (already implemented)
- Custom queries with `@Query` (optional bonus)

### Configuration
See `application.properties`:
- H2 database setup
- JPA settings
- Enable SQL logging for debugging

### H2 Console
Access at http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:library`
- Username: `sa`
- Password: (empty)

## Getting Started

### 1. Fork the Repository
1. Go to the GitHub repository
2. Click "Fork" to create your team's copy
3. Clone your fork:
```bash
git clone https://github.com/YOUR-USERNAME/assignment-4-solid.git
cd assignment-4-solid
```

### 2. Verify Everything Works
```bash
./gradlew test
# All 11 tests should pass
# Current coverage: 62%
```

### 3. Understand the Violations
- Read through `LibraryService.java`
- Identify each SOLID violation
- Plan your refactoring strategy

### 4. Refactor Incrementally
1. Start with TODO 1 (BookService)
2. Ensure tests still pass
3. Move to TODO 2 (CheckoutPolicy)
4. Continue one TODO at a time
5. Run tests after each change

### 5. Run the Application
```bash
./gradlew bootRun
# Access H2 console at http://localhost:8080/h2-console
```

## Grading Rubric

| Component | Points | Requirements |
|-----------|--------|--------------|
| TODO 1 - BookService | 15 | Proper SRP separation, all book operations extracted |
| TODO 2 - CheckoutPolicy | 15 | Strategy pattern, OCP compliance, factory implemented |
| TODO 3 - NotificationService | 10 | Interface abstraction, DIP demonstrated |
| TODO 4 - MemberService | 15 | SRP compliance, clear responsibilities |
| TODO 5 - LateFeeCalculator | 10 | Strategy pattern for fees |
| TODO 6 - BookSearchService | 10 | ISP compliance, focused interface |
| TODO 7 - ReportGenerator | 10 | Strategy for reports, easily extensible |
| TODO 8 - Integration & Docs | 5 | LibraryFacade, updated tests |
| **Team Collaboration** | **10** | **All members contribute, git history shows distribution** |
| **Total** | **100** | |

### Quality Criteria
- All tests pass ✓
- 80%+ code coverage ✓
- No SOLID violations remaining ✓
- Code is well-organized ✓
- Javadoc for public APIs ✓

## AI Collaboration Requirements

Document at the top of `LibraryFacade.java`:

```java
/**
 * AI Collaboration Summary:
 *
 * Team Members and Contributions:
 * - Alice Smith: TODOs 1, 2, 3
 * - Bob Jones: TODOs 4, 5
 * - Carol Kim: TODOs 6, 7, 8
 *
 * AI Tools Used: [ChatGPT/Claude/Copilot/Gemini/etc.]
 *
 * How AI Helped:
 * - [Describe key ways AI assisted with the refactoring]
 * - [e.g., "Suggested Strategy pattern structure for checkout policies"]
 *
 * What We Learned:
 * - [Key insights about SOLID principles]
 * - [Which principle was hardest to apply and why]
 */
```

## Common Pitfalls

❌ **DON'T:**
- Extract services but keep them tightly coupled
- Create interfaces without implementation flexibility
- Ignore the existing tests
- Make too many changes at once
- Forget to inject dependencies via constructors

✅ **DO:**
- Make one change at a time
- Run tests after each change
- Use constructor injection (@RequiredArgsConstructor works great)
- Keep strategies stateless when possible
- Write new tests for new services

## Documentation Requirements

### REFACTORING.md

Create a `REFACTORING.md` document that explains your refactoring decisions. For each SOLID principle you addressed:

1. **Identify the violation** - What was wrong in the original code?
2. **Explain your fix** - How did you refactor it?
3. **Show code examples** - Before/after snippets demonstrating the change
4. **Justify the design** - Why is this better?

**Template:**

```markdown
# Refactoring Report: SOLID Principles

## Single Responsibility Principle (SRP)

### Violation
The original `LibraryService` class had multiple responsibilities:
- Book operations (checkout, return)
- Member operations (update checkout counts)
- Notifications (sending checkout/return messages)
- Search operations (finding books)
- Reporting (generating reports)

### Our Solution
We extracted separate services, each with a single responsibility:
- `BookService` - manages book state and operations
- `MemberService` - manages member state and operations
- `NotificationService` - handles all notifications
- `BookSearchService` - performs search operations

### Code Example

**Before:**
```java
public class LibraryService {
    public void checkoutBook(String isbn, String memberEmail) {
        // Book logic
        // Member logic
        // Notification logic
        // All mixed together!
    }
}
```

**After:**
```java
public class BookService {
    public void checkoutBook(Book book, Member member, int loanPeriodDays) {
        // Only book-related operations
        book.setStatus(BookStatus.CHECKED_OUT);
        book.setCheckedOutBy(member.getEmail());
        book.setDueDate(LocalDate.now().plusDays(loanPeriodDays));
        bookRepository.save(book);
    }
}
```

### Why This Is Better
- Each service can be tested independently
- Changes to one concern don't affect others
- Easier to understand and maintain
- Services can be reused in different contexts

---

## Open-Closed Principle (OCP)

### Violation
The original code used if-else statements to handle different membership types:
```java
if (member.getMembershipType() == MembershipType.REGULAR) {
    maxBooks = 3;
} else if (member.getMembershipType() == MembershipType.PREMIUM) {
    maxBooks = 10;
} // Adding new types requires modifying this code!
```

### Our Solution
We implemented the Strategy pattern with `CheckoutPolicy`:
[Explain your implementation...]

### Code Example
[Show before/after code...]

### Why This Is Better
[Explain the benefits...]

---

## Liskov Substitution Principle (LSP)

[If applicable, explain how your refactoring supports proper inheritance...]

---

## Interface Segregation Principle (ISP)

### Violation
[Explain how the monolithic service would force clients to depend on unused methods...]

### Our Solution
[Explain how separate, focused services allow clients to depend only on what they need...]

---

## Dependency Inversion Principle (DIP)

### Violation
The original code depended directly on concrete implementations (System.out.println):
```java
System.out.println("Book checked out: " + book.getTitle());
```

### Our Solution
We created a `NotificationService` interface and depend on the abstraction:
[Explain your implementation...]

### Code Example
[Show before/after code...]

### Why This Is Better
[Explain the benefits...]

---

## Summary

- **Lines of code changed:** [Approximate number]
- **New classes/interfaces created:** [List them]
- **Test coverage improvement:** 62% → [Your coverage]%
- **Most challenging principle:** [Which one and why]
- **Key learning:** [What did you learn about SOLID principles?]
```

Use this template as a starting point. Feel free to add diagrams, additional examples, or other insights.

## Submission Requirements

1. **One submission per team** - submit your fork's URL to Moodle
2. Ensure `./gradlew test` passes with 80%+ coverage
3. Include completed AI Collaboration documentation in `LibraryFacade.java`
4. Include completed `REFACTORING.md` document (see template above)
5. **Verify all team members show in git history**: GitHub → Insights → Contributors

## Tips for Success

1. **Understand SOLID First**
   - Review Week 6 slides
   - Study `examples/design-patterns/solid/`
   - Identify violations before coding

2. **Test-Driven Refactoring**
   - Keep tests green
   - Refactor when tests pass
   - Add new tests for new services

3. **Use Design Patterns**
   - Strategy for variable behavior
   - Factory for object creation
   - Facade for coordination

4. **Leverage Spring**
   - Constructor injection
   - Component scanning
   - Spring manages your objects

5. **Document Decisions**
   - Why you chose Strategy over Template Method
   - Trade-offs of your design
   - What you would do differently

## Resources

- Week 6 Slides: SOLID Principles
- `examples/design-patterns/solid/` - Working SOLID examples
- Spring Data JPA Documentation
- Design Patterns: Strategy, Factory, Facade
- Office Hours: Wednesdays 1:30-3:00 PM

## Academic Integrity

This is a **team assignment**. Collaboration within teams is expected and encouraged. However:
- Each team's solution must be original
- You may discuss concepts with other teams
- You may NOT share code with other teams
- Document all AI assistance used

## Extra Challenges (Optional)

1. **Add Transaction Management**
   - Use `@Transactional` appropriately
   - Handle rollback scenarios

2. **Implement Real Email Service**
   - Use Spring Mail
   - Make it configurable (dev vs prod)

3. **Add More Membership Types**
   - Demonstrate OCP in action
   - No modifications to existing code

4. **Create REST Controllers**
   - Build on your facade
   - Add API documentation

5. **Implement Observer Pattern**
   - Notify when books become available
   - Demonstrate loose coupling

---

**Remember:** The goal isn't just to make tests pass—it's to understand **why** SOLID principles lead to better software design. Focus on creating maintainable, extensible, and testable code!

Good luck! 🚀
