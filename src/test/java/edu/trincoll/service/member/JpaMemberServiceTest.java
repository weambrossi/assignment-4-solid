package edu.trincoll.service.member;

import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JpaMemberService Unit Tests")
class JpaMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private JpaMemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("John Doe", "john@example.com", MembershipType.REGULAR);
        member.setMemberSince(LocalDate.of(2020, 1, 1));
        member.setBooksCheckedOut(2);
    }

    @Test
    @DisplayName("getMemberByEmail: returns existing member")
    void getMemberByEmailReturnsMember() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));

        Member result = memberService.getMemberByEmail(member.getEmail());

        assertThat(result).isSameAs(member);
    }

    @Test
    @DisplayName("getMemberByEmail: throws when not found")
    void getMemberByEmailThrowsWhenMissing() {
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberByEmail(member.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member not found");
    }

    @Test
    @DisplayName("incrementBooksCheckedOut: increments counter and persists")
    void incrementBooksCheckedOut() {
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member result = memberService.incrementBooksCheckedOut(member);

        assertThat(result.getBooksCheckedOut()).isEqualTo(3);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("decrementBooksCheckedOut: decrements counter with floor at zero")
    void decrementBooksCheckedOut() {
        member.setBooksCheckedOut(0);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member result = memberService.decrementBooksCheckedOut(member);

        assertThat(result.getBooksCheckedOut()).isZero();
        verify(memberRepository).save(member);
    }
}
