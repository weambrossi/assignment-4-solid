package edu.trincoll.repository;

import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import jakarta.websocket.OnError;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PremiumCheckoutPolicy implements CheckoutPolicy {
    @Override
    public int getMaxBooks() {return 10; }

    @Override
    public int getLoanPeriodDays() {return 30;}

    @Override
    public boolean canCheckout(Member member) {
        return member.getBooksCheckedOut() < getMaxBooks();
    }

    @Override
    public MembershipType getMembershipType() {
        return MembershipType.PREMIUM;
    }

}
