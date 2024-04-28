package com.shinkevich.mailclientcourseproject.Model.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.shinkevich.mailclientcourseproject.Model.Database.Entity.MailEntity;

@Database(entities =
        {MailEntity.class},
        version = 22)
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
