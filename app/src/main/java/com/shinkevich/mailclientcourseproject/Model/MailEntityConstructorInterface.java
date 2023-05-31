package com.shinkevich.mailclientcourseproject.Model;

public interface MailEntityConstructorInterface<T> {
    T createEntity(long messageUID,
                   String authorEmail,
                   String authorName,
                   String recipientEmail,
                   String topic,
                   String content,
                   String date,
                   Boolean isRead,
                   Boolean isInFavourites);
}
