package com.shinkevich.mailclientcourseproject.Model.Database.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.shinkevich.mailclientcourseproject.Model.MailType;

@Entity(tableName = "deferred_mail")
public class DeferredMail extends MailEntity {
//    @PrimaryKey(autoGenerate = true)
//    private int mailNumber;
    @PrimaryKey
    private int requestCode;
    public DeferredMail(String authorEmail,
                        String authorName,
                        String recipientEmail,
                        String topic,
                        String content,
                        String date,
                        Boolean isRead,
                        Boolean isInFavourites,
                        int requestCode ) {
        super(requestCode, authorEmail, authorName,recipientEmail, topic, content, date, isRead, isInFavourites, MailType.DEFERRED.toString());
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }
}