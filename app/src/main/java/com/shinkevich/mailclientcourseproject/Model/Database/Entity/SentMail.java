package com.shinkevich.mailclientcourseproject.Model.Database.Entity;

import androidx.room.Entity;

import com.shinkevich.mailclientcourseproject.Model.MailType;

@Entity(tableName = "sent_mail")
public class SentMail extends MailEntity{
    public SentMail(long messageUID,
                    String authorEmail,
                    String authorName,
                    String recipientEmail,
                    String topic,
                    String content,
                    String date,
                    Boolean isRead,
                    Boolean isInFavourites) {
        super(messageUID, authorEmail, authorName,recipientEmail, topic, content, date, isRead, isInFavourites, MailType.SENT.toString());
    }
}
