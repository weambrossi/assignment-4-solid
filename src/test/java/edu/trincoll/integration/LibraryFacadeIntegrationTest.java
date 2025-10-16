package edu.trincoll.integration;

import edu.trincoll.model.Book;
import edu.trincoll.model.BookStatus;
import edu.trincoll.model.Member;
import edu.trincoll.model.MembershipType;
import edu.trincoll.repository.BookRepository;
import edu.trincoll.repository.MemberRepository;
import edu.trincoll.service.LibraryFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("LibraryFacade Integration Tests")
class LibraryFacadeIntegrationTest {

    @Autowired
    private LibraryFacade libraryFacade;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        memberRepository.deleteAll();

        book = new Book("978-0-321-49805-2", "Clean Architecture", "Robert Martin",
                LocalDate.of(2017, 9, 20));
        book.setStatus(BookStatus.AVAILABLE);
        book = bookRepository.save(book);

        member = new Member("Integration Tester", "itester@example.com", MembershipType.REGULAR);
        member.setBooksCheckedOut(0);
        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("Full checkout and return flow updates persistent state")
    void checkoutAndReturnFlow() {
        LocalDate today = LocalDate.now();

        String checkoutMessage = libraryFacade.checkoutBook(book.getIsbn(), member.getEmail());
        assertThat(checkoutMessage).contains("Book checked out successfully");

        Book checkedOutBook = bookRepository.findByIsbn(book.getIsbn()).orElseThrow();
        assertThat(checkedOutBook.getStatus()).isEqualTo(BookStatus.CHECKED_OUT);
        assertThat(checkedOutBook.getCheckedOutBy()).isEqualTo(member.getEmail());
        assertThat(checkedOutBook.getDueDate()).isEqualTo(today.plusDays(14));

        Member updatedMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(updatedMember.getBooksCheckedOut()).isEqualTo(1);

        checkedOutBook.setDueDate(today.minusDays(2));
        bookRepository.save(checkedOutBook);

        String returnMessage = libraryFacade.returnBook(book.getIsbn());
        assertThat(returnMessage).isEqualTo("Book returned. Late fee: $1.00");

        Book returnedBook = bookRepository.findByIsbn(book.getIsbn()).orElseThrow();
        assertThat(returnedBook.getStatus()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(returnedBook.getCheckedOutBy()).isNull();
        assertThat(returnedBook.getDueDate()).isNull();

        Member returnedMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(returnedMember.getBooksCheckedOut()).isZero();
    }
}
