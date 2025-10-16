package edu.trincoll.service;

import edu.trincoll.model.Book;
import edu.trincoll.model.Member;

import java.time.LocalDate;

public interface NotificationService {
    String sendCheckoutNotification(Member member, Book book, LocalDate dueDate);
    String sendReturnNotification(Member member, Book book, double lateFee);
}
