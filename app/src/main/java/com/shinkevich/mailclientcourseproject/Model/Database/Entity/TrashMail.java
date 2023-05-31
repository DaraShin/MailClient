package com.shinkevich.mailclientcourseproject.Model.Database.Entity;

import androidx.room.Entity;

@Entity(tableName = "trash_mail")
public class TrashMail extends MailEntity {
    public TrashMail(long messageUID,
                     String authorEmail,
                     String authorName,
                     String recipientEmail,
                     String topic,
                     String content,
                     String date,
                     Boolean isRead,
                     Boolean isInFavourites) {
        super(messageUID, authorEmail, authorName, recipientEmail, topic, content, date, isRead, isInFavourites, "trash");
    }
}
