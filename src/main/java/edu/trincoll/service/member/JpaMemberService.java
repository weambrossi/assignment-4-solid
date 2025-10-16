package edu.trincoll.service.member;

import edu.trincoll.model.Member;
import edu.trincoll.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class JpaMemberService implements MemberService {

    private final MemberRepository memberRepository;

    public JpaMemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Member incrementBooksCheckedOut(Member member) {
        member.setBooksCheckedOut(member.getBooksCheckedOut() + 1);
        return save(member);
    }

    @Override
    public Member decrementBooksCheckedOut(Member member) {
        member.setBooksCheckedOut(Math.max(0, member.getBooksCheckedOut() - 1));
        return save(member);
    }

    @Override
    public long countMembers() {
        return memberRepository.count();
    }
}
