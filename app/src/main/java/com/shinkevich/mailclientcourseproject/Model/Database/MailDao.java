package com.shinkevich.mailclientcourseproject.Model.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shinkevich.mailclientcourseproject.Model.Database.Entity.DeferredMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.Draft;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.IncomingMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.SentMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.SpamMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.TrashMail;

import java.util.List;

@Dao
public interface MailDao {
    @Insert
    void insertIncomingMail(IncomingMail mail);

    @Update
    void updateIncomingMail(IncomingMail mail);

    @Delete
    void deleteIncomingMail (IncomingMail mail);

    @Query("SELECT * FROM incoming_mail")
    List<IncomingMail> getAllIncomingMails();

    @Query("SELECT * FROM incoming_mail WHERE messageUID = :pMessageUid")
    List<IncomingMail> getIncomingMailById(long pMessageUid);

    @Query("SELECT * FROM incoming_mail WHERE isInFavourites = 1")
    List<IncomingMail> getFavouriteIncomingMails();

    // sent mails
    @Insert
    void insertSentMail(SentMail mail);

    @Update
    void updateSentMail(SentMail mail);

    @Delete
    void deleteSentMail (SentMail mail);

    @Query("SELECT * FROM sent_mail")
    List<SentMail> getAllSentMails();

    @Query("SELECT * FROM sent_mail WHERE messageUID = :pMessageUid")
    List<SentMail> getSentMailById(long pMessageUid);

    @Query("SELECT * FROM sent_mail WHERE isInFavourites = 1")
    List<SentMail> getFavouriteSentMails();

    // draft mails
    @Insert
    void insertDraftMail(Draft mail);

    @Update
    void updateDraftMail(Draft mail);

    @Delete
    void deleteDraftMail (Draft mail);

    @Query("SELECT * FROM draft")
    List<Draft> getAllDraftMails();

    @Query("SELECT * FROM draft WHERE messageUID = :pMessageUid")
    List<Draft> getDraftMailById(long pMessageUid);

    @Query("SELECT * FROM draft WHERE isInFavourites = 1")
    List<Draft> getFavouriteDraftMails();

    // favourite mails
//    @Insert
//    void insertFavouriteMail(FavouriteMail mail);
//
//    @Delete
//    void deleteFavouriteMail(FavouriteMail mail);
//
//    @Update
//    void updateFavouriteMail(FavouriteMail mail);
//
//    @Query("SELECT * FROM favourite_mail")
//    List<FavouriteMail> getAllFavouriteMails();
//
//    @Query("SELECT * FROM favourite_mail WHERE messageUID = :pMessageUid")
//    List<FavouriteMail> getFavouriteMailById(long pMessageUid);
//
//    @Query("SELECT * FROM favourite_mail WHERE messageUID = :pMessageUid AND messageType = :pMessageType")
//    List<FavouriteMail> getFavouriteMail(long pMessageUid, String pMessageType);

//    @Query("SELECT * FROM favourite_mail WHERE date = :pDate AND authorEmail = :pAuthorEmail AND topic = :pTopic AND content = :pContent")
//    List<FavouriteMail> findFavouriteMail(String pDate, String pAuthorEmail, String pTopic, String pContent);

    // spam mails
    @Insert
    void insertSpamMail(SpamMail mail);

    @Update
    void updateSpamMail(SpamMail mail);

    @Delete
    void deleteSpamMail (SpamMail mail);

    @Query("SELECT * FROM spam_mail")
    List<SpamMail> getAllSpamMails();

    @Query("SELECT * FROM spam_mail WHERE messageUID = :pMessageUid")
    List<SpamMail> getSpamMailById(long pMessageUid);

    @Query("SELECT * FROM spam_mail WHERE isInFavourites = 1")
    List<SpamMail> getFavouriteSpamMails();

    // trash mails
    @Insert
    void insertTrashMail(TrashMail mail);

    @Update
    void updateTrashMail(TrashMail mail);

    @Delete
    void deleteTrashMail (TrashMail mail);

    @Query("SELECT * FROM trash_mail")
    List<TrashMail> getAllTrashMails();

    @Query("SELECT * FROM trash_mail WHERE messageUID = :pMessageUid")
    List<TrashMail> getTrashMailById(long pMessageUid);

    // deferred mails
    @Insert
    void insertDeferredMail(DeferredMail mail);

    @Delete
    void deleteDeferredMail(DeferredMail mail);

    @Update
    void updateDeferredMail(DeferredMail mail);

    @Query("SELECT * FROM deferred_mail")
    List<DeferredMail> getAllDeferredMails();

    @Query("SELECT * FROM deferred_mail WHERE requestCode = :pRequestCode")
    List<DeferredMail> getDeferredMailByRequestCode(int pRequestCode);

    @Query("SELECT * FROM deferred_mail WHERE isInFavourites = 1")
    List<DeferredMail> getFavouriteDeferredMails();
}