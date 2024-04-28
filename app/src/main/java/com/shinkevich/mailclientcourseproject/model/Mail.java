package com.shinkevich.mailclientcourseproject.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Mail implements Serializable {
    private String mailID;
    private long messageUID;
    private String authorEmail;
    private String authorName;
    private String topic;
    private String text;
    private String date;
    private Boolean isRead;
    private Boolean isInFavourites;
    private List<String> recipients = new ArrayList<>();

    private MailType mailType;


    public Mail() {
        isRead = true;
        isInFavourites = false;
    }

    public String getMailID() {
        return mailID;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getAuthorName() {
        return authorName;
    }

    public long getMessageUID() {
        return messageUID;
    }

    public String getTopic() {
        return topic;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public Boolean isRead() {
        return isRead;
    }

    public Boolean isInFavourites() {
        return isInFavourites;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public void setMessageUID(long messageUID) {
        this.messageUID = messageUID;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public void setIsInFavourites(Boolean isInFavourites) {
        this.isInFavourites = isInFavourites;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public void addRecipient(String recipient) {
        if (this.recipients == null) {
            recipients = new ArrayList<>();
        }
        recipients.add(recipient);
    }

    public MailType getMailType() {
        return mailType;
    }

    public void setMailType(MailType mailType) {
        this.mailType = mailType;
    }
}
