package edu.trincoll.service.fee;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;

import java.time.LocalDate;

public interface LateFeeCalculator {

    boolean supports(MembershipType type);

    double calculateFee(Book book, Member member, LocalDate returnDate);
}
