package com.shinkevich.mailclientcourseproject.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.shinkevich.mailclientcourseproject.model.database.entity.MailEntity;
import com.shinkevich.mailclientcourseproject.model.database.MailDao;
import com.shinkevich.mailclientcourseproject.model.database.MailsDatabase;
import com.shinkevich.mailclientcourseproject.view.WriteMessageActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Repository {
    private Context context;

    private MailServerConnector mailServerConnector;
    private MailDao mailDao;
    private AccountManager accountManager;

    private ExecutorService executorService;

    public Repository(Context context) {
        this.context = context;
        mailDao = MailsDatabase.getInstance(context).mailDao();
        mailServerConnector = new MailServerConnector(context);
        executorService = Executors.newSingleThreadExecutor();
        accountManager = new AccountManager(context);
    }

    public Single<List<Mail>> getLocalMails(MailType mailType) {
        return Single.fromCallable(() -> {
            List<MailEntity> mailEntitiesFromDB = mailDao.getMailsByType(mailType);
            List<Mail> mailsFromDb = new ArrayList<>(mailEntitiesFromDB.size());
            for (MailEntity mailEntity : mailEntitiesFromDB) {
                Mail mail = mailEntityToMail(mailEntity);
                mailsFromDb.add(mail);
            }
            return mailsFromDb;
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<Mail>> getMailsFromServer(MailType mailType) {
        return Single.fromCallable(() -> {
            List<Mail> mails = mailServerConnector.getMails(mailType);
            if (accountManager.needLogin()) {
                return new ArrayList<Mail>();
            }
            for (Mail mail : mails) {
                mail.setMailType(mailType);
                List<MailEntity> mailsByIdList = mailDao.getMailByPK(mail.getMailID(), mail.getMailType());
                MailEntity mailEntity = mailToMailEntity(mail);
                if (mailsByIdList.size() == 0) {
                    mailDao.insertMail(mailEntity);
                } else {
                    mailEntity.setRead(mailsByIdList.get(0).getRead());
                    mail.setIsRead(mailsByIdList.get(0).getRead());

                    mailEntity.setInFavourites(mailsByIdList.get(0).getInFavourites());
                    mail.setIsInFavourites(mailsByIdList.get(0).getInFavourites());

                    mailDao.updateMail(mailEntity);
                }
            }
            return mails;
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Mail>> getMails(MailType mailType) {
        switch (mailType) {
            case INCOMING:
            case SENT: ;
            case DRAFT:
            case SPAM:
                return Observable.merge(
                                Observable.fromSingle(getLocalMails(mailType)),
                                Observable.fromSingle(getMailsFromServer(mailType))
                        )
                        .take(2)
                        .subscribeOn(Schedulers.io());
            default:
                return Observable.error(new Exception("Invalid mail type"));
        }
    }

    public Observable<List<Mail>> getFavourites() {
        return Observable.fromCallable(() -> {
            List<MailEntity> favouriteMailsFromDB = mailDao.getFavouriteMails();
            List<Mail> favouriteMails = new ArrayList<>(favouriteMailsFromDB.size());
            for (MailEntity mailEntity : favouriteMailsFromDB) {
                Mail mail = mailEntityToMail(mailEntity);
                favouriteMails.add(mail);
            }
            return favouriteMails;
        }).subscribeOn(Schedulers.io());
    }


    /*public LiveData<List<Mail>> getIncomingMails() {
        getIncomingMessagesAsync();
        return incomingMailsLiveData;
    }
    private void getIncomingMessagesAsync() {
        CompletableFuture.runAsync(() -> {
            ((MutableLiveData<List<Mail>>) incomingMailsLiveData).postValue(mailServerConnector.getIncomingMails());
        }, executorService);
    }*/

    public Single<Boolean> sendMessage(Mail mail, String receiver) {
        return Single.fromCallable(() -> {
                    return mailServerConnector.sendMessage(mail, receiver);
                })
                .subscribeOn(Schedulers.io());
    }

    public Completable saveDraft(Mail mail) {
        System.out.println("---- uid: " + mail.getMessageUID());
        System.out.println("---- id: " + mail.getMailID());
        return Completable.fromCallable(() -> {
            System.out.println("------- in new");
            // save new draft
            MailServerConnector.DraftInfo draftInfo = mailServerConnector.saveDraft(mail);
            Mail draftMail = draftInfo.getDraftMail();
            if (draftInfo.getDraftMail().getMessageUID() == -1) {
                throw new Exception("Error while saving draft");
            } else {
                // save draft locally
                MailEntity draft = mailToMailEntity(draftMail);
                draft.setMessageType(MailType.DRAFT);
                mailDao.insertMail(draft);
            }
            if (!mailDao.getMailByPK(mail.getMailID(), MailType.DRAFT).isEmpty()) {
                System.out.println("!!!!!!!!!! delete caled");
                deleteMail(mail).blockingSubscribe();
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    public Single<Boolean> checkAuthenticationData(String login, String password) {
        return Single.fromCallable(() -> {
                    return mailServerConnector.checkAuthenticationData(login, password);
                })
                .subscribeOn(Schedulers.io());
    }

    public void markMailAsFavourite(Mail mail) {
        Completable.fromRunnable(() -> {
                    List<MailEntity> mailsByPK = mailDao.getMailByPK(mail.getMailID(), mail.getMailType());
                    if (!mailsByPK.isEmpty()) {
                        MailEntity mailEntity = mailsByPK.get(0);
                        mailEntity.setInFavourites(mail.isInFavourites());
                        mailDao.updateMail(mailEntity);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public Single<Mail> getMailByPK(String mailID, MailType mailType) {
        return Single.fromCallable(() -> {
                    List<MailEntity> mailEntityList = mailDao.getMailByPK(mailID, mailType);
                    if (mailEntityList.size() == 0) {
                        return null;
                    } else {
                        MailEntity mailEntity = mailEntityList.get(0);
                        return mailEntityToMail(mailEntity);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Observable<Mail> getDraftMailByIdForTest(String mailID) {
        return Observable.fromCallable(() -> {
                    List<MailEntity> mailEntityList = mailDao.getMailByPK(mailID, MailType.DRAFT);
                    if (mailEntityList.size() == 0) {
                        return null;
                    } else {
                        MailEntity mailEntity = mailEntityList.get(0);
                        return mailEntityToMail(mailEntity);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Mail getDeferredMailByRequestCodeSync(int requestCode) {
        List<MailEntity> mailEntityList = mailDao.getMailByPK(String.valueOf(requestCode), MailType.DEFERRED);
        if (mailEntityList.size() == 0) {
            return null;
        } else {
            MailEntity deferredMail = mailEntityList.get(0);
            return mailEntityToMail(deferredMail);
        }
    }

    public void setMailIsSeen(Mail mail) {
        if (mail.getMailType().equals(MailType.DEFERRED)) {
            return;
        }
        Completable.mergeArray(
                        // update on server
                        Completable.fromRunnable(() -> {
                            mailServerConnector.sendMailIsSeen(mail);
                        }),
                        // update in database
                        Completable.fromRunnable(() -> {
                            List<MailEntity> mails = mailDao.getMailByPK(mail.getMailID(),mail.getMailType());
                            if (mails.size() > 0) {
                                MailEntity mailEntity = mails.get(0);
                                mailEntity.setRead(mail.isRead());
                                mailDao.updateMail(mailEntity);
                            }
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void addDeferredMailToDB(Mail mail, int requestCode) {
        Completable.fromRunnable(() -> {
                    mail.setMailID(String.valueOf(requestCode));
                    mail.setMailType(MailType.DEFERRED);
                    MailEntity deferredMail = mailToMailEntity(mail);
                    mailDao.insertMail(deferredMail);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteDeferredMailSync(int requestCode) {
        List<MailEntity> deferredMailList = mailDao.getMailByPK(String.valueOf(requestCode), MailType.DEFERRED);
        if (deferredMailList.isEmpty()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DeferredSendBroadcastReceiver.class);
        intent.setAction(WriteMessageActivity.DEFERRED_SEND_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, Integer.valueOf(deferredMailList.get(0).getMailID()), intent, PendingIntent.FLAG_IMMUTABLE | Intent.FILL_IN_DATA | PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        mailDao.deleteMail(deferredMailList.get(0));
    }

    public Completable deleteDeferredMail(int requestCode) {
        return Completable.fromRunnable(() -> {
                    deleteDeferredMailSync(requestCode);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void clearDB() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                MailsDatabase.getInstance(context).clearAllTables();
            }
        });
    }

    public Future clearDBForTest() {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                MailsDatabase.getInstance(context).clearAllTables();
            }
        });
    }

    private Completable deleteLocalMail(Mail mail) {
        switch (mail.getMailType()) {
            case INCOMING:
            case SENT:
            case DRAFT:
            case SPAM:
                return Completable.fromRunnable(() -> {
                List<MailEntity> mailEntityList = mailDao.getMailByPK(mail.getMailID(), mail.getMailType());
                if (mailEntityList.isEmpty()) {
                    return;
                }
                mailDao.deleteMail(mailEntityList.get(0));
            }).subscribeOn(Schedulers.io());
            case DEFERRED:
                return deleteDeferredMail((int) mail.getMessageUID());
            default:
                return Completable.error(new Exception("Unknown mail type"));
        }
    }

    public Completable deleteMail(Mail mail) {
        Completable deleteFromServerCompletable = Completable.fromCallable(() -> {
            mailServerConnector.deleteMail(mail);
            return null;
        }).subscribeOn(Schedulers.io());
        switch (mail.getMailType()) {
            case INCOMING:
            case SENT:
            case DRAFT:
            case SPAM:
                return Completable.mergeArray(deleteFromServerCompletable, deleteLocalMail(mail));
            case DEFERRED:
                return deleteLocalMail(mail);
            default:
                return Completable.complete();
        }
    }

    private Mail mailEntityToMail(MailEntity mailEntity) {
        Mail mail = new Mail();
        mail.setMailID(mailEntity.getMailID());
        mail.setMessageUID(mailEntity.getMessageUID());
        mail.setAuthorEmail(mailEntity.getAuthorEmail());
        mail.setAuthorName(mailEntity.getAuthorName());
        mail.addRecipient(mailEntity.getRecipientEmail());
        mail.setTopic(mailEntity.getTopic());
        mail.setText(mailEntity.getContent());
        mail.setDate(mailEntity.getDate());
        mail.setIsRead(mailEntity.getRead());
        mail.setIsInFavourites(mailEntity.getInFavourites());
        mail.setMailType(mailEntity.getMessageType());
        return mail;
    }

    private MailEntity mailToMailEntity(Mail mail) {
        MailEntity mailEntity = new MailEntity(
                mail.getMailID(),
                mail.getMessageUID(),
                mail.getAuthorEmail(),
                mail.getAuthorName(),
                mail.getRecipients().isEmpty() ? "" : mail.getRecipients().get(0),
                mail.getTopic(),
                mail.getText(),
                mail.getDate(),
                mail.isRead(),
                mail.isInFavourites(),
                mail.getMailType()
        );
        return mailEntity;
    }
}
