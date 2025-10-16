package edu.trincoll.service.notification;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;

public interface NotificationService {

    void notifyCheckout(Member member, Book book);

    void notifyReturn(Member member, Book book, double lateFee);
}
