package edu.trincoll.service.policy;

import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class RegularCheckoutPolicy implements CheckoutPolicy {

    @Override
    public boolean supports(MembershipType type) {
        return MembershipType.REGULAR == type;
    }

    @Override
    public int getMaxBooks() {
        return 3;
    }

    @Override
    public int getLoanPeriodDays() {
        return 14;
    }
}
