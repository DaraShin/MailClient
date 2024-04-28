package com.shinkevich.mailclientcourseproject.model.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.shinkevich.mailclientcourseproject.model.MailType;

@Entity(tableName = "mail", primaryKeys = {"mail_id","mail_type"})
public class MailEntity {
    @NonNull
    @ColumnInfo(name = "mail_id")
    private String mailID;
    private long messageUID;
    private String authorEmail;
    private String authorName;
    private String recipientEmail;
    private String topic;
    private String content;
    private String date;
    private Boolean isRead;
    private Boolean isInFavourites;
    @NonNull
    @ColumnInfo(name = "mail_type")
    private MailType messageType;


    public MailEntity(String mailID,
                      long messageUID,
                      String authorEmail,
                      String authorName,
                      String recipientEmail,
                      String topic,
                      String content,
                      String date,
                      Boolean isRead,
                      Boolean isInFavourites,
                      MailType messageType) {
        this.mailID = mailID;
        this.messageUID = messageUID;
        this.authorEmail = authorEmail;
        this.authorName = authorName;
        this.recipientEmail = recipientEmail;
        this.topic = topic;
        this.content = content;
        this.date = date;
        this.isRead = isRead;
        this.isInFavourites = isInFavourites;
        this.messageType = messageType;
    }

    public String getMailID() {
        return mailID;
    }

    public long getMessageUID() {
        return messageUID;
    }

    public void setMessageUID(long messageUID) {
        this.messageUID = messageUID;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Boolean getInFavourites() {
        return isInFavourites;
    }

    public void setInFavourites(Boolean inFavourites) {
        isInFavourites = inFavourites;
    }

    public MailType getMessageType() {
        return messageType;
    }

    public void setMessageType(MailType messageType) {
        this.messageType = messageType;
    }
}