package edu.trincoll.service.latefee;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class LateFeeCalculatorFactory {
    public LateFeeCalculator getCalculator(MembershipType membershipType) {
        switch (membershipType) {
            case REGULAR:
                return new RegularLateFeeCalculator();
            case PREMIUM:
                return new PremiumLateFeeCalculator();
            case STUDENT:
                return new StudentLateFeeCalculator();
            default:
                throw new IllegalArgumentException("Unknown membership type: " + membershipType);
        }
    }
}