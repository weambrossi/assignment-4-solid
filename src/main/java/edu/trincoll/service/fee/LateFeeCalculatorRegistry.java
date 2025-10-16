package edu.trincoll.service.fee;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LateFeeCalculatorRegistry {

    private final List<LateFeeCalculator> calculators;

    public LateFeeCalculatorRegistry(List<LateFeeCalculator> calculators) {
        this.calculators = calculators;
    }

    public double calculateFee(MembershipType membershipType, Book book, Member member, LocalDate returnDate) {
        return calculators.stream()
                .filter(calculator -> calculator.supports(membershipType))
                .findFirst()
                .map(calculator -> calculator.calculateFee(book, member, returnDate))
                .orElseThrow(() -> new IllegalStateException("Unknown membership type: " + membershipType));
    }
}
