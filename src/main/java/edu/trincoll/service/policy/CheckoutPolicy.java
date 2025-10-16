package edu.trincoll.service.policy;

import edu.trincoll.model.MembershipType;

public interface CheckoutPolicy {

    boolean supports(MembershipType type);

    int getMaxBooks();

    int getLoanPeriodDays();

    default boolean canCheckout(int currentBooksCheckedOut) {
        return currentBooksCheckedOut < getMaxBooks();
    }
}
