package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CheckoutPolicyRegistry {

    private final List<CheckoutPolicy> policies;

    public CheckoutPolicyRegistry(List<CheckoutPolicy> policies) {
        this.policies = policies;
    }

    public CheckoutPolicy resolvePolicy(MembershipType membershipType) {
        return policies.stream()
                .filter(policy -> policy.supports(membershipType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown membership type: " + membershipType));
    }
}
