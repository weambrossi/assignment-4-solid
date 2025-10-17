package edu.trincoll.service;

import edu.trincoll.model.Member;
import edu.trincoll.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member getByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    public Member incrementCheckedOut(Member member) {
        member.setBooksCheckedOut(member.getBooksCheckedOut() + 1);
        return memberRepository.save(member);
    }

    public Member decrementCheckedOut(Member member) {
        if (member.getBooksCheckedOut() > 0) {
            member.setBooksCheckedOut(member.getBooksCheckedOut() - 1);
        }
        return memberRepository.save(member);
    }
}
