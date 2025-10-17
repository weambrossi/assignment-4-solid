package edu.trincoll.service;

import edu.trincoll.model.Member;
import edu.trincoll.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService")
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @InjectMocks private MemberService memberService;

    @Test
    @DisplayName("getByEmail retrieves member or throws")
    void getByEmail_returnsMember() {
        Member member = new Member();
        when(memberRepository.findByEmail("reader@example.com"))
                .thenReturn(Optional.of(member));

        Member result = memberService.getByEmail("reader@example.com");

        assertThat(result).isSameAs(member);
        verify(memberRepository).findByEmail("reader@example.com");
    }

    @Test
    @DisplayName("getByEmail throws when not found")
    void getByEmail_throwsWhenMissing() {
        when(memberRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getByEmail("missing@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member not found");
    }

    @Test
    @DisplayName("incrementCheckedOut increases counter and saves")
    void incrementCheckedOut() {
        Member member = new Member();
        member.setBooksCheckedOut(1);

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member updated = memberService.incrementCheckedOut(member);

        assertThat(updated.getBooksCheckedOut()).isEqualTo(2);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("decrementCheckedOut decreases counter but not below zero")
    void decrementCheckedOut() {
        Member member = new Member();
        member.setBooksCheckedOut(0);

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member updated = memberService.decrementCheckedOut(member);
        assertThat(updated.getBooksCheckedOut()).isZero();

        member.setBooksCheckedOut(2);
        Member updatedAgain = memberService.decrementCheckedOut(member);
        assertThat(updatedAgain.getBooksCheckedOut()).isEqualTo(1);
        verify(memberRepository, times(2)).save(member);
    }
}
