package com.shinkevich.mailclientcourseproject.Model;

import android.content.Context;
import android.util.Log;

//import com.sun.mail.gimap.GmailFolder;
//import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailServerConnector {
    private static final String gmailImapHost = "imap.gmail.com";
    private static final String imapPort = "993";
    private static final String imapMailStoreType = "imap";
    private static final String gmailSmtpHost = "smtp.gmail.com";
    private static final String smtpPort = "587";

    private static Properties gmailImapProperties = new Properties();
    private static Properties gmailSmtpProperties = new Properties();
    private static Properties mailRuImapProperties = new Properties();
    private static Properties mailRuSmtpProperties = new Properties();

    private AccountManager accountManager;

    public MailServerConnector(Context context) {
        accountManager = new AccountManager(context);
    }

    static {
        gmailImapProperties.put("mail.imap.host", gmailImapHost);
        gmailImapProperties.put("mail.imap.port", imapPort);
        gmailImapProperties.put("mail.imap.auth", "true");
        gmailImapProperties.put("mail.imap.ssl.enable", "true");
        gmailImapProperties.put("mail.imap.ssl.protocols", "TLSv1.2");

        gmailSmtpProperties.put("mail.smtp.host", gmailSmtpHost);
        gmailSmtpProperties.put("mail.smtp.port", smtpPort);
        gmailSmtpProperties.put("mail.smtp.auth", "true");
        gmailSmtpProperties.put("mail.smtp.starttls.enable", "true");
        gmailSmtpProperties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        mailRuImapProperties.put("mail.imap.host", gmailImapHost);
        mailRuImapProperties.put("mail.imap.port", imapPort);
        mailRuImapProperties.put("mail.imap.auth", "true");
        mailRuImapProperties.put("mail.imap.ssl.enable", "true");
        mailRuImapProperties.put("mail.imap.ssl.protocols", "TLSv1.2");

        mailRuSmtpProperties.put("mail.smtp.host", gmailSmtpHost);
        mailRuSmtpProperties.put("mail.smtp.port", smtpPort);
        mailRuSmtpProperties.put("mail.smtp.auth", "true");
        mailRuSmtpProperties.put("mail.smtp.starttls.enable", "true");
        mailRuSmtpProperties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
    }

    public List<Mail> getIncomingMails() {
        MailServiceEnum mailService = accountManager.getActiveUser().getMailService();
        switch (mailService) {
            case GMAIL:
                return getMessagesFromFolder("INBOX");
            default:
                return null;
        }
    }

    public List<Mail> getSentMails() {
        MailServiceEnum mailService = accountManager.getActiveUser().getMailService();
        switch (mailService) {
            case GMAIL:
                return getMessagesFromFolder("[Gmail]/Отправленные");
            default:
                return null;
        }
    }

    public List<Mail> getDrafts() {
        MailServiceEnum mailService = accountManager.getActiveUser().getMailService();
        switch (mailService) {
            case GMAIL:
                return getMessagesFromFolder("[Gmail]/Черновики");
            default:
                return null;
        }
    }

    /*public List<Mail> getFavouriteMails() {
        MailServiceEnum mailService = accountManager.getActiveUser().getMailService();
        switch (mailService) {
            case GMAIL:
                return getMessagesFromFolder("[Gmail]/Помеченные");
            default:
                return null;
        }
    }*/

    public List<Mail> getSpam() {
        MailServiceEnum mailService = accountManager.getActiveUser().getMailService();
        switch (mailService) {
            case GMAIL:
                return getMessagesFromFolder("[Gmail]/Спам");
            default:
                return null;
        }
    }

    /*public List<Mail> getTrash() {
        return getMessagesFromFolder("[Gmail]/Корзина");
    }

    public List<Mail> getUnseen() {
        return null;
    }*/

    private Properties getImapProperties(MailServiceEnum mailService) {
        switch (mailService) {
            case GMAIL:
                return gmailImapProperties;
            case MAIL_RU:
                return mailRuImapProperties;
        }
        return null;
    }

    private Properties getSmtpProperties(MailServiceEnum mailService) {
        switch (mailService) {
            case GMAIL:
                return gmailSmtpProperties;
            case MAIL_RU:
                return mailRuSmtpProperties;
        }
        return null;
    }

    private List<Mail> getMessagesFromFolder(String folder) {
        AccountManager.User user = accountManager.getActiveUser();
        Session emailSession = Session.getDefaultInstance(getImapProperties(user.getMailService()));

        List<Mail> mailsList = null;
        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(imapMailStoreType);
            emailStore.connect(user.getEmail(), user.getPassword());

            Folder emailFolder = emailStore.getFolder(folder);
            UIDFolder uidFolder = (UIDFolder) emailFolder;
            emailFolder.open(Folder.READ_ONLY);

            mailsList = new ArrayList<>(emailFolder.getMessageCount());
            Mail nextMail;
            Message[] messages = emailFolder.getMessages();

            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                nextMail = new Mail();
                nextMail.setMessageUID(uidFolder.getUID(message));
                if (message.getFrom().length != 0) {
                    nextMail.setAuthorEmail(((InternetAddress) message.getFrom()[0]).getAddress());
                    nextMail.setAuthorName(((InternetAddress) message.getFrom()[0]).getPersonal());
                }
                if (message.getAllRecipients() != null && message.getAllRecipients().length > 0) {
                    nextMail.addRecipient(((InternetAddress) (message.getAllRecipients()[0])).getAddress());
                }

                nextMail.setTopic(message.getSubject());
                nextMail.setText(getMessageString(message));
                nextMail.setDate(message.getReceivedDate().toString());
                nextMail.setIsRead(message.isSet(Flags.Flag.SEEN));
                //nextMail.setIsInFavourites(message.isSet(Flags.Flag.FLAGGED));
                nextMail.setIsInFavourites(false);

                mailsList.add(nextMail);
            }

            emailFolder.close(false);
            emailStore.close();
        } catch (NoSuchProviderException e) {
            Log.i("", Log.getStackTraceString(e));
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.i("", Log.getStackTraceString(e));
        }
        return mailsList;
    }

    private String getMessageString(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            String text = "";
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    //return text + bodyPart.getContent() + "\n";
                    text += bodyPart.getContent();
                }
            }
            return text;
        }
        return "";
    }

    public boolean sendMessage(Mail mail, String receiver) {
        AccountManager.User user = accountManager.getActiveUser();
        Session session = Session.getInstance(gmailSmtpProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user.getEmail(), user.getPassword());
            }
        });

        MimeMessage message = null;
        try {
            message = new MimeMessage(session);
            //message.setFrom(new InternetAddress(user));
            message.setFrom(new InternetAddress(user.getEmail()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
            message.setSubject(mail.getTopic());
            message.setText(mail.getText());

            Transport.send(message);
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
            return false;
        }
        return true;
    }

    private Mail getMailFromMessage(Message message, long uid) throws MessagingException {
        Mail mail = new Mail();
        mail.setMessageUID(uid);
        if (message.getFrom().length != 0) {
            mail.setAuthorEmail(((InternetAddress) message.getFrom()[0]).getAddress());
            mail.setAuthorName(((InternetAddress) message.getFrom()[0]).getPersonal());
        }
        mail.setTopic(message.getSubject());
        try {
            mail.setText(getMessageString(message));
        } catch (IOException e) {
            Log.i("", Log.getStackTraceString(e));
            mail.setText("Can't get message text");
        }

        mail.setDate(message.getReceivedDate().toString());
        mail.setIsRead(message.isSet(Flags.Flag.SEEN));
        //nextMail.setIsInFavourites(message.isSet(Flags.Flag.FLAGGED));
        mail.setIsInFavourites(false);
        return mail;
    }

    public DraftInfo saveDraft(Mail mail) {
        AccountManager.User user = accountManager.getActiveUser();
        Session emailSession = Session.getDefaultInstance(gmailImapProperties);

        List<Mail> mailsList = null;
        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(imapMailStoreType);
            emailStore.connect(user.getEmail(), user.getPassword());

            Folder emailFolder = emailStore.getFolder("[Gmail]/Черновики");
            emailFolder.open(Folder.READ_WRITE);

            Message message = new MimeMessage(emailSession);
            message.setSubject(mail.getTopic());
            message.setText(mail.getText());
            message.setFrom(new InternetAddress(user.getEmail()));
            message.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(user.getEmail())});
            message.setFlags(new Flags(Flags.Flag.DRAFT), true);
            emailFolder.appendMessages(new Message[]{message});

            Message addedDraft = emailFolder.getMessage(emailFolder.getMessageCount());
            UIDFolder uidFolder = (UIDFolder) emailFolder;
            long draftUID = uidFolder.getUID(addedDraft);

            DraftInfo draftInfo = new DraftInfo(draftUID, addedDraft);
            emailFolder.close(false);
            emailStore.close();

            return draftInfo;
        } catch (NoSuchProviderException e) {
            Log.i("", Log.getStackTraceString(e));
            return new DraftInfo(-1, null);
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
            return new DraftInfo(-1, null);
        }
    }

    public boolean updateDraft(Mail mail) {
        AccountManager.User user = accountManager.getActiveUser();
        Session emailSession = Session.getDefaultInstance(gmailImapProperties);

        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(imapMailStoreType);
            emailStore.connect(user.getEmail(), user.getPassword());

            if (emailStore.hasCapability("UIDPLUS")) {
                System.out.println("Server supports UIDPLUS");
            } else {
                System.out.println("Server does not support UIDPLUS");
            }

            Folder emailFolder = emailStore.getFolder("[Gmail]/Черновики");
            UIDFolder uidFolder = (UIDFolder)emailFolder;
            emailFolder.open(Folder.READ_WRITE);

            Message draftMessage = uidFolder.getMessageByUID(mail.getMessageUID());
            if (draftMessage == null) {
                return false;
            }
            draftMessage.setSubject(mail.getTopic());
            draftMessage.setText(mail.getText());
            draftMessage.setFrom(new InternetAddress(user.getEmail()));
            draftMessage.setRecipients(Message.RecipientType.TO, new Address[]{new InternetAddress(user.getEmail())});
            draftMessage.setFlags(new Flags(Flags.Flag.DRAFT), true);

            emailFolder.close(false);
            emailStore.close();

            return true;
        } catch (NoSuchProviderException e) {
            Log.i("", Log.getStackTraceString(e));
            return false;
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean checkAuthenticationData(String login, String password) {
        Session emailSession = Session.getDefaultInstance(gmailImapProperties);
        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(imapMailStoreType);
            emailStore.connect(login, password);
            emailStore.close();
        } catch (AuthenticationFailedException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        }
        return true;
    }

    private String getFolderByMailType(MailType mailType) {
        switch (mailType) {
            case INCOMING:
                return "INBOX";
            case SENT:
                return "[Gmail]/Отправленные";
            case DRAFT:
                return "[Gmail]/Черновики";
            case FAVOURITE:
                return "[Gmail]/Помеченные";
            case SPAM:
                return "[Gmail]/Спам";
            default:
                return "";
        }
    }

    /*public void markMailAsFavourite(Mail mail) {
        String mailStoreType = "imap";

        Session emailSession = Session.getDefaultInstance(gmailImapProperties);

        List<Mail> mailsList = null;
        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(mailStoreType);
            emailStore.connect(user, password);

            String folder = getFolderByMailType(mail.getMailType());
            Folder emailFolder = emailStore.getFolder(folder);
            emailFolder.open(Folder.READ_WRITE);
            UIDFolder uidFolder = (UIDFolder) emailFolder;

            Message message = uidFolder.getMessageByUID(mail.getMessageUID());
            //message.setFlags(new Flags(Flags.Flag.FLAGGED), mail.isInFavourites());
            //message.saveChanges();


            //--------------------------------------------------

            /*FetchProfile fp = new FetchProfile();
            fp.add(GmailFolder.FetchProfileItem.LABELS);
            Message[] msgs = new Message[200];
            emailFolder.fetch(msgs, fp);

            for (Message m : msgs) {
                GmailMessage im = (GmailMessage) m;
                String[] labels =im.getLabels();
                if(labels!=null){
                    for(String label: labels){
                        System.out.println("Label: " + label);
                    }
                }
            }*/
    //--------------------------------------------------

            /*emailFolder = emailStore.getFolder("[Gmail]/Помеченные");
            emailFolder.open(Folder.READ_WRITE);
            uidFolder = (UIDFolder) emailFolder;
            if (mail.isInFavourites()){
                emailFolder.appendMessages(new Message[]{message});
            } else {
                message = uidFolder.getMessageByUID(favouriteMail.getMessageUID());
                message.setFlag(Flags.Flag.DELETED, true);
            }*


            emailFolder.close(false);
            emailStore.close();
        } catch (NoSuchProviderException e) {
            Log.i("", Log.getStackTraceString(e));
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
        }
        Log.i("", "------------------------ end ");
    }*/

    public void sendMailIsSeen(Mail mail) {
        AccountManager.User user = accountManager.getActiveUser();
        Session emailSession = Session.getDefaultInstance(gmailImapProperties);

        List<Mail> mailsList = null;
        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(imapMailStoreType);
            emailStore.connect(user.getEmail(), user.getPassword());

            String folder = getFolderByMailType(mail.getMailType());
            Folder emailFolder = emailStore.getFolder(folder);
            emailFolder.open(Folder.READ_WRITE);
            UIDFolder uidFolder = (UIDFolder) emailFolder;

            Message message = uidFolder.getMessageByUID(mail.getMessageUID());
            if (message == null) {
                return;
            }

            message.setFlags(new Flags(Flags.Flag.SEEN), mail.isRead());

            emailFolder.close(false);
            emailStore.close();
        } catch (NoSuchProviderException e) {
            Log.i("", Log.getStackTraceString(e));
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
        }
    }

    public void deleteMail(Mail mail) {
        AccountManager.User user = accountManager.getActiveUser();
        Session emailSession = Session.getDefaultInstance(gmailImapProperties);
        try {
            IMAPStore emailStore = (IMAPStore) emailSession.getStore(imapMailStoreType);
            emailStore.connect(user.getEmail(), user.getPassword());

            String folder = getFolderByMailType(mail.getMailType());
            Folder emailFolder = emailStore.getFolder(folder);
            emailFolder.open(Folder.READ_WRITE);
            UIDFolder uidFolder = (UIDFolder) emailFolder;

            Message message = uidFolder.getMessageByUID(mail.getMessageUID());
            if (message == null) {
                return;
            }
            message.setFlags(new Flags(Flags.Flag.DELETED), true);

            emailFolder.close(false);
            emailStore.close();
        } catch (NoSuchProviderException e) {
            Log.i("", Log.getStackTraceString(e));
        } catch (MessagingException e) {
            Log.i("", Log.getStackTraceString(e));
        }
    }

    class DraftInfo {
        private long draftUid;
        private Mail draftMail;

        public DraftInfo(long draftUid, Message draftMessage) {
            this.draftUid = draftUid;
            try {
                this.draftMail = getMailFromMessage(draftMessage, draftUid);
            } catch (Exception e) {
                e.printStackTrace();
                this.draftMail = new Mail();
                draftMail.setMessageUID(draftUid);
            }
        }

        public Mail getDraftMail() {
            return draftMail;
        }

    }

}
