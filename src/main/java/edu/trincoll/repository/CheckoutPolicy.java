package edu.trincoll.repository;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

public interface CheckoutPolicy {
    int getMaxBooks();

    int getLoanPeriodDays();

    boolean canCheckout(Member member);

    MembershipType getMembershipType();
}

