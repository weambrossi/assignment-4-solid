package edu.trincoll.repository;

import edu.trincoll.model.MembershipType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CheckoutPolicyFactory")
class CheckoutPolicyFactoryTest {

    @Test
    void returnsRegisteredPolicy() {
        CheckoutPolicyFactory factory = new CheckoutPolicyFactory(List.of(
                new RegularCheckoutPolicy(),
                new PremiumCheckoutPolicy(),
                new StudentCheckoutPolicy()
        ));
        CheckoutPolicy policy = factory.getCheckoutPolicy(MembershipType.PREMIUM);
        assertThat(policy.getMembershipType()).isEqualTo(MembershipType.PREMIUM);
        assertThat(policy.getMaxBooks()).isEqualTo(10);
    }

    @Test
    void rejectsUnknownType() {
        CheckoutPolicyFactory factory = new CheckoutPolicyFactory(List.of(
                new RegularCheckoutPolicy(),
                new PremiumCheckoutPolicy()
        ));

        assertThatThrownBy(() -> factory.getCheckoutPolicy(MembershipType.STUDENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown checkout policy type");
    }
}
