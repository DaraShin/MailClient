package com.shinkevich.mailclientcourseproject.ViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.shinkevich.mailclientcourseproject.Model.AccountManager;
import com.shinkevich.mailclientcourseproject.Model.Mail;
import com.shinkevich.mailclientcourseproject.Model.MailType;
import com.shinkevich.mailclientcourseproject.Model.Repository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class ShowMailsListViewModel extends AndroidViewModel {
    private Repository repository;
//    private MailsListAdapter incomingAdapter;
//    private MailsListAdapter sentAdapter;
//    private MailsListAdapter delayedAdapter;
//    private MailsListAdapter draftsAdapter;
//    private MailsListAdapter favouritesAdapter;
//    private MailsListAdapter spamAdapter;

    private MutableLiveData<List<Mail>> incomingMailsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Mail>> sentMailsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Mail>> draftsMailsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Mail>> favouritesMailsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Mail>> spamMailsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Mail>> deferredMailsLiveData = new MutableLiveData<>();

    private MutableLiveData<Integer> incomingMailsLoading = new MutableLiveData<>(0);
    private MutableLiveData<Integer> sentMailsLoading = new MutableLiveData<>(0);
    private MutableLiveData<Integer> draftsMailsLoading = new MutableLiveData<>(0);
    private MutableLiveData<Integer> favouritesMailsLoading = new MutableLiveData<>(0);
    private MutableLiveData<Integer> spamMailsLoading = new MutableLiveData<>(0);
    private MutableLiveData<Integer> deferredMailsLoading = new MutableLiveData<>(0);

    private Context context;

    public ShowMailsListViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        this.context = application;
        //((MutableLiveData)incomingMailsLiveData).setValue(new ArrayList<>());
    }

    private MutableLiveData<List<Mail>> getMailsListLiveDataByType(MailType mailType) {
        switch (mailType) {
            case INCOMING:
                return incomingMailsLiveData;
            case SENT:
                return sentMailsLiveData;
            case DRAFT:
                return draftsMailsLiveData;
            case SPAM:
                return spamMailsLiveData;
            case FAVOURITE:
                return favouritesMailsLiveData;
            case DEFERRED:
                return deferredMailsLiveData;
            default:
                return null;
        }
    }

    private MutableLiveData<Integer> getLoadingLiveDataByType(MailType mailType) {
        switch (mailType) {
            case INCOMING:
                return incomingMailsLoading;
            case SENT:
                return sentMailsLoading;
            case DRAFT:
                return draftsMailsLoading;
            case SPAM:
                return spamMailsLoading;
            case FAVOURITE:
                return favouritesMailsLoading;
            case DEFERRED:
                return deferredMailsLoading;
            default:
                return null;
        }
    }

    /*public LiveData<List<Mail>> getLocalMails(MailType mailType) {
        MutableLiveData<List<Mail>> liveData = getLiveDataByType(mailType);
        repository.getLocalMails(mailType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    liveData.postValue(mailsList);
                });
        return liveData;
    }*/

    public LiveData<List<Mail>> getMailsFromServer(MailType mailType) {
        MutableLiveData<List<Mail>> mailsListLiveData = getMailsListLiveDataByType(mailType);
        MutableLiveData<Integer> loadingLiveData = getLoadingLiveDataByType(mailType);

        repository.getMailsFromServer(mailType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Mail>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        loadingLiveData.setValue(loadingLiveData.getValue() + 1);
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Mail> mailsList) {
                        mailsListLiveData.setValue(mailsList);
                        loadingLiveData.setValue(loadingLiveData.getValue() - 1);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        loadingLiveData.setValue(loadingLiveData.getValue() - 1);
                    }
                });
//                .subscribe(mailsList -> {
//                    mailsListLiveData.postValue(mailsList);
//                });
        return mailsListLiveData;
    }

    public LiveData<List<Mail>> getMails(MailType mailType) {
        MutableLiveData<List<Mail>> mailsLiveData = getMailsListLiveDataByType(mailType);
        MutableLiveData<Integer> loadingLiveData = getLoadingLiveDataByType(mailType);
        if (mailsLiveData == null) {
            return null;
        }
        if (mailsLiveData.getValue() == null) {
            repository.getMails(mailType)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Mail>>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                            loadingLiveData.setValue(loadingLiveData.getValue() + 1);
                        }
                        @Override
                        public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<Mail> mailsList) {
                            mailsLiveData.setValue(mailsList);
                        }
                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            loadingLiveData.setValue(loadingLiveData.getValue() - 1);
                        }
                        @Override
                        public void onComplete() {
                            loadingLiveData.setValue(loadingLiveData.getValue() - 1);
                        }
                    });
        } else {
            repository.getLocalMails(mailType)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mailsList -> {
                        mailsLiveData.setValue(mailsList);
                    });
        }
        return mailsLiveData;
    }

    public LiveData<List<Mail>> getIncomingMails() {
        repository.getMails(MailType.INCOMING)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    incomingMailsLiveData.setValue(mailsList);
                });
        return incomingMailsLiveData;
    }

    public LiveData<List<Mail>> getSentMails() {
        repository.getMails(MailType.SENT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    sentMailsLiveData.setValue(mailsList);
                });
        return sentMailsLiveData;
    }

    public LiveData<List<Mail>> getDrafts() {
        repository.getMails(MailType.DRAFT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    draftsMailsLiveData.setValue(mailsList);
                });
        return draftsMailsLiveData;
    }

    public LiveData<List<Mail>> getFavourites() {
        repository.getFavourites()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    favouritesMailsLiveData.setValue(mailsList);
                });
        return favouritesMailsLiveData;
    }

    public LiveData<List<Mail>> getDeferred() {
        repository.getLocalMails(MailType.DEFERRED)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    deferredMailsLiveData.setValue(mailsList);
                });
        return deferredMailsLiveData;
    }

    public LiveData<List<Mail>> getSpam() {
        repository.getMails(MailType.SPAM)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mailsList -> {
                    spamMailsLiveData.setValue(mailsList);
                });
        return spamMailsLiveData;
    }


//    public MailsListAdapter getDelayedAdapter(Context context) {
//        if (delayedAdapter == null) {
//            List<Mail> messagesList = serverConnector.getDelayed();
//            //delayedAdapter = new MailsListAdapter(context, (ArrayList<Mail>) messagesList);
//        }
//        return delayedAdapter;
//    }

    public void favouriteClick(Mail mail) {
        //serverConnector.addFavourite(message);
        repository.markMailAsFavourite(mail);
    }

    public void setMessageSeen(Mail mail) {
        repository.setMailIsSeen(mail);
    }

    public void clearDataOnLogout() {
        new AccountManager(context).logout();
        repository.clearDB();
    }

    public MutableLiveData<Integer> getIncomingMailsLoading() {
        return incomingMailsLoading;
    }

    public MutableLiveData<Integer> getSentMailsLoading() {
        return sentMailsLoading;
    }

    public MutableLiveData<Integer> getDraftsMailsLoading() {
        return draftsMailsLoading;
    }

    public MutableLiveData<Integer> getFavouritesMailsLoading() {
        return favouritesMailsLoading;
    }

    public MutableLiveData<Integer> getSpamMailsLoading() {
        return spamMailsLoading;
    }

    public MutableLiveData<Integer> getDeferredMailsLoading() {
        return deferredMailsLoading;
    }
}
