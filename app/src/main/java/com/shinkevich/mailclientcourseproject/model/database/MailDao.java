package com.shinkevich.mailclientcourseproject.model.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shinkevich.mailclientcourseproject.model.database.entity.MailEntity;
import com.shinkevich.mailclientcourseproject.model.MailType;

import java.util.List;

@Dao
public interface MailDao {
    @Insert
    void insertMail(MailEntity mail);

    @Update
    void updateMail(MailEntity mail);

    @Delete
    void deleteMail (MailEntity mail);

    @Query("SELECT * FROM mail WHERE mail_type = :mailType")
    List<MailEntity> getMailsByType(MailType mailType);

    @Query("SELECT * FROM mail WHERE mail_id = :mailId AND mail_type = :mailType")
    List<MailEntity> getMailByPK(String mailId, MailType mailType);

    @Query("SELECT * FROM mail WHERE isInFavourites = 1")
    List<MailEntity> getFavouriteMails();

    @Query("DELETE FROM mail")
    void clear();
}