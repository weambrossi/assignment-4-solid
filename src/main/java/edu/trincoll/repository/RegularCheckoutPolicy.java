package edu.trincoll.repository;

import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import org.springframework.stereotype.Component;

@Component
public class RegularCheckoutPolicy implements CheckoutPolicy {
        @Override
        public int getMaxBooks() {
            return 3;
        }

        @Override
        public int getLoanPeriodDays() {
            return 14;
        }

        @Override
        public boolean canCheckout(Member member) {
            return member.getBooksCheckedOut() < getMaxBooks();
        }

        @Override
        public MembershipType getMembershipType() {
            return MembershipType.REGULAR;
        }

    }
