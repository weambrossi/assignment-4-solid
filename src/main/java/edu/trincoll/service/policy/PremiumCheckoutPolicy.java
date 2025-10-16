package edu.trincoll.service.policy;

import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class PremiumCheckoutPolicy implements CheckoutPolicy {

    @Override
    public boolean supports(MembershipType type) {
        return MembershipType.PREMIUM == type;
    }

    @Override
    public int getMaxBooks() {
        return 10;
    }

    @Override
    public int getLoanPeriodDays() {
        return 30;
    }
}
