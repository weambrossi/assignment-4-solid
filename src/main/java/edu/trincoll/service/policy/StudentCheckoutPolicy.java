package edu.trincoll.service.policy;

import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class StudentCheckoutPolicy implements CheckoutPolicy {

    @Override
    public boolean supports(MembershipType type) {
        return MembershipType.STUDENT == type;
    }

    @Override
    public int getMaxBooks() {
        return 5;
    }

    @Override
    public int getLoanPeriodDays() {
        return 21;
    }
}
