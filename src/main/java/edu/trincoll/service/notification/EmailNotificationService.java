package edu.trincoll.service.notification;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public void notifyCheckout(Member member, Book book) {
        log.info("Email to {}: '{}' checked out. Due {}", member.getEmail(), book.getTitle(), book.getDueDate());
    }

    @Override
    public void notifyReturn(Member member, Book book, double lateFee) {
        if (lateFee > 0) {
            log.info("Email to {}: '{}' returned. Late fee ${}", member.getEmail(), book.getTitle(), String.format("%.2f", lateFee));
        } else {
            log.info("Email to {}: '{}' returned. No late fee", member.getEmail(), book.getTitle());
        }
    }
}
