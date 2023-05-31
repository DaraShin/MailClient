package com.shinkevich.mailclientcourseproject.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.shinkevich.mailclientcourseproject.Model.Mail;
import com.shinkevich.mailclientcourseproject.Model.Repository;

import io.reactivex.rxjava3.core.Completable;

public class ReadMessageViewModel extends AndroidViewModel {
    Repository repository;
    Context context;

    public ReadMessageViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        this.context = application;
    }

//    public Maybe<Mail> getMessage(long messageUID, String table_name){
//        System.out.println("in view model uid: " + messageUID);
//        switch(table_name){
//            case "incoming_mail":
//                return repository.getIncomingMailById(messageUID);
//            case "sent_mail":
//                return repository.getSentMailById(messageUID);
//            case "draft":
//                return repository.getDraftMailById(messageUID);
//            case "favourite_mail":
//                return repository.getFavouriteMailById(messageUID);
//            case "spam_mail":
//                return repository.getSpamMailById(messageUID);
//            default:
//                return Maybe.just(null);
//        }
//    }

    public void favouriteClick(Mail mail) {
        //serverConnector.addFavourite(message);
        repository.markMailAsFavourite(mail);
    }

    public Completable deleteMail(Mail mail){
        return repository.deleteMail(mail);
    }
}
