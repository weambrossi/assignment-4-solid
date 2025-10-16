package edu.trincoll.service.fee;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LateFeeCalculatorRegistry Tests")
class LateFeeCalculatorRegistryTest {

    private final LateFeeCalculatorRegistry registry = new LateFeeCalculatorRegistry(
            List.of(new RegularLateFeeCalculator(), new PremiumLateFeeCalculator(), new StudentLateFeeCalculator())
    );

    private Book overdueBook;
    private Member regularMember;

    @BeforeEach
    void setUp() {
        overdueBook = new Book("978-0-98765-43-2", "Refactoring", "Martin Fowler", LocalDate.of(1999, 7, 8));
        overdueBook.setStatus(BookStatus.CHECKED_OUT);
        overdueBook.setDueDate(LocalDate.now().minusDays(4));

        regularMember = new Member("John Reader", "reader@example.com", MembershipType.REGULAR);
    }

    @Test
    @DisplayName("calculateFee: uses matching calculator for regular members")
    void calculateFeeForRegularMember() {
        double fee = registry.calculateFee(MembershipType.REGULAR, overdueBook, regularMember, LocalDate.now());

        assertThat(fee).isEqualTo(2.0);
    }

    @Test
    @DisplayName("calculateFee: premium members incur no fee")
    void calculateFeeForPremiumMember() {
        double fee = registry.calculateFee(MembershipType.PREMIUM, overdueBook, regularMember, LocalDate.now());

        assertThat(fee).isZero();
    }
}
