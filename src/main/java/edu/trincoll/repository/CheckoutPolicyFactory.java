package edu.trincoll.repository;

import edu.trincoll.model.MembershipType;
import org.aspectj.apache.bcel.generic.RET;

import java.util.Map;

public class CheckoutPolicyFactory {
    private static final Map<MembershipType, CheckoutPolicy> checkoutPolicies = Map.of(
            MembershipType.REGULAR, new RegularCheckoutPolicy(),
            MembershipType.PREMIUM, new PremiumCheckoutPolicy(),
            MembershipType.STUDENT, new StudentCheckoutPolicy()
    );

    public static CheckoutPolicy getCheckoutPolicy(MembershipType type) {
        CheckoutPolicy policy = checkoutPolicies.get(type);
        if(policy == null) {
            throw new IllegalArgumentException("Unknown checkout policy type: " + type);
        }
        return policy;
    }
}
