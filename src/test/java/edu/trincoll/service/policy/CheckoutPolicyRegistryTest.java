package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CheckoutPolicyRegistry Tests")
class CheckoutPolicyRegistryTest {

    private final CheckoutPolicyRegistry registry = new CheckoutPolicyRegistry(
            List.of(new RegularCheckoutPolicy(), new PremiumCheckoutPolicy(), new StudentCheckoutPolicy())
    );

    @Test
    @DisplayName("resolvePolicy: returns policy matching membership type")
    void resolvePolicyReturnsCorrectImplementation() {
        CheckoutPolicy policy = registry.resolvePolicy(MembershipType.PREMIUM);

        assertThat(policy.supports(MembershipType.PREMIUM)).isTrue();
        assertThat(policy.getMaxBooks()).isEqualTo(10);
        assertThat(policy.getLoanPeriodDays()).isEqualTo(30);
    }

    @Test
    @DisplayName("resolvePolicy: throws for unknown membership type")
    void resolvePolicyThrowsForUnknown() {
        assertThatThrownBy(() -> registry.resolvePolicy(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown membership type");
    }
}
