package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void sendCheckoutNotification(Member member, Book book, LocalDate dueDate) {
        System.out.println("Sending email to: " + member.getEmail());
        System.out.println("Subject: Book checked out");
        System.out.println("Message: You have checked out: " + book.getTitle());
        System.out.println("Book checked out successfully: " + dueDate);
    }

    @Override
    public void sendReturnNotification(Member member, Book book, double lateFee) {
        System.out.println("Sending email to: " + member.getEmail());
        System.out.println("Subject: Book returned");
        if (lateFee > 0) {
            System.out.println("Book returned. Late fee: $ " + String.format("%.2f", lateFee));
        }
        System.out.println("Message: You have returned: " + book.getTitle());
    }
}
