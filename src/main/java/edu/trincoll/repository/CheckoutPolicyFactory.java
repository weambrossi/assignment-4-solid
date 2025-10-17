package edu.trincoll.repository;

import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CheckoutPolicyFactory {

    private final Map<MembershipType, CheckoutPolicy> policiesByType;

    public CheckoutPolicyFactory(java.util.List<CheckoutPolicy> policies) {
        this.policiesByType = policies.stream()
                .collect(Collectors.toUnmodifiableMap(CheckoutPolicy::getMembershipType, Function.identity()));
    }

    public CheckoutPolicy getCheckoutPolicy(MembershipType type) {
        CheckoutPolicy policy = policiesByType.get(type);
        if (policy == null) {
            throw new IllegalArgumentException("Unknown checkout policy type: " + type);
        }
        return policy;
    }
}
