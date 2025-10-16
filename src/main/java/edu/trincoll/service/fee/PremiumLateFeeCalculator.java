package edu.trincoll.service.fee;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PremiumLateFeeCalculator implements LateFeeCalculator {

    @Override
    public boolean supports(MembershipType type) {
        return MembershipType.PREMIUM == type;
    }

    @Override
    public double calculateFee(Book book, Member member, LocalDate returnDate) {
        return 0.0;
    }
}
