package com.shinkevich.mailclientcourseproject.Model.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.shinkevich.mailclientcourseproject.Model.Database.Entity.DeferredMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.Draft;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.IncomingMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.SentMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.SpamMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.TrashMail;

@Database(entities =
        {IncomingMail.class, SentMail.class, Draft.class, TrashMail.class, SpamMail.class, DeferredMail.class},
        version = 21)
public abstract class MailsDatabase extends RoomDatabase {
    private static MailsDatabase instance;

    public abstract MailDao mailDao();

    public static synchronized MailsDatabase getInstance(Context context){
        if(instance==null){
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MailsDatabase.class,
                    "mails_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
