package edu.trincoll.service.fee;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StudentLateFeeCalculator implements LateFeeCalculator {

    private static final double DAILY_FEE = 0.25;

    @Override
    public boolean supports(MembershipType type) {
        return MembershipType.STUDENT == type;
    }

    @Override
    public double calculateFee(Book book, Member member, LocalDate returnDate) {
        if (book.getDueDate() == null || !returnDate.isAfter(book.getDueDate())) {
            return 0.0;
        }
        long daysLate = returnDate.toEpochDay() - book.getDueDate().toEpochDay();
        return daysLate * DAILY_FEE;
    }
}
