package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailNotificationService")
class EmailNotificationServiceTest {

    private final EmailNotificationService notificationService = new EmailNotificationService();

    @Test
    void sendsCheckoutMessage() {
        Member member = new Member();
        member.setEmail("reader@example.com");
        Book book = new Book();
        book.setTitle("Clean Code");
        LocalDate dueDate = LocalDate.of(2024, 10, 15);

        String output = captureOutput(() ->
                notificationService.sendCheckoutNotification(member, book, dueDate));

        assertThat(output)
                .contains("Sending email to: reader@example.com")
                .contains("Book checked out successfully: 2024-10-15");
    }

    @Test
    void sendsReturnMessageWithLateFee() {
        Member member = new Member();
        member.setEmail("reader@example.com");
        Book book = new Book();
        book.setTitle("Clean Code");

        String output = captureOutput(() ->
                notificationService.sendReturnNotification(member, book, 1.50));

        assertThat(output)
                .contains("Subject: Book returned")
                .contains("Book returned. Late fee: $ 1.50")
                .contains("You have returned: Clean Code");
    }

    private static String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}

