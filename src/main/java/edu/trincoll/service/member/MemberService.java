package edu.trincoll.service.member;

import edu.trincoll.model.Member;

public interface MemberService {

    Member getMemberByEmail(String email);

    Member save(Member member);

    Member incrementBooksCheckedOut(Member member);

    Member decrementBooksCheckedOut(Member member);

    long countMembers();
}
