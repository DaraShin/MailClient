package com.shinkevich.mailclientcourseproject.Model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.work.ListenableWorker;

import com.shinkevich.mailclientcourseproject.Model.Database.MailDao;
import com.shinkevich.mailclientcourseproject.Model.Database.MailsDatabase;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.DeferredMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.Draft;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.IncomingMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.MailEntity;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.SentMail;
import com.shinkevich.mailclientcourseproject.Model.Database.Entity.SpamMail;
import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.View.WriteMessageActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        switch (mailType) {
            case INCOMING:
                return getLocalMails(mailDao::getAllIncomingMails);
            case SENT:
                return getLocalMails(mailDao::getAllSentMails);
            case DRAFT:
                return getLocalMails(mailDao::getAllDraftMails);
            case SPAM:
                return getLocalMails(mailDao::getAllSpamMails);
            case DEFERRED:
                return getLocalMails(mailDao::getAllDeferredMails);
            default:
                return Single.error(new Throwable("Unknown mail type"));
        }
    }

    private <T extends MailEntity> Single<List<Mail>> getLocalMails(
            Supplier<List<T>> mailsFromDBGetter) {
        return Single.fromCallable(() -> {
            List<T> mailEntitiesFromDB = mailsFromDBGetter.get();
            List<Mail> mailsFromDb = new ArrayList<>(mailEntitiesFromDB.size());
            for (T mailEntity : mailEntitiesFromDB) {
                Mail mail = mailEntityToMail(mailEntity);
                mailsFromDb.add(mail);
            }
            return mailsFromDb;
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<Mail>> getMailsFromServer(MailType mailType) {
        switch (mailType) {
            case INCOMING:
                return getMailsFromServer(mailServerConnector::getIncomingMails,
                        mailDao::getIncomingMailById,
                        mailDao::insertIncomingMail,
                        mailDao::updateIncomingMail,
                        IncomingMail::new,
                        MailType.INCOMING);
            case SENT:
                return getMailsFromServer(mailServerConnector::getSentMails,
                        mailDao::getSentMailById,
                        mailDao::insertSentMail,
                        mailDao::updateSentMail,
                        SentMail::new,
                        MailType.SENT);
            case DRAFT:
                return getMailsFromServer(mailServerConnector::getDrafts,
                        mailDao::getDraftMailById,
                        mailDao::insertDraftMail,
                        mailDao::updateDraftMail,
                        Draft::new,
                        MailType.DRAFT);
            case SPAM:
                return getMailsFromServer(mailServerConnector::getSpam,
                        mailDao::getSpamMailById,
                        mailDao::insertSpamMail,
                        mailDao::updateSpamMail,
                        SpamMail::new,
                        MailType.SPAM);
            default:
                return Single.error(new Throwable("Unknown mail type"));
        }
    }

    private <T extends MailEntity> Single<List<Mail>> getMailsFromServer(Supplier<List<Mail>> mailsFromServerGetter,
                                                                         LongFunction<List<T>> mailsByIdGetter,
                                                                         Consumer<T> toDBInserter,
                                                                         Consumer<T> dbUpdater,
                                                                         MailEntityConstructorInterface<T> constructor,
                                                                         MailType mailType) {
        return Single.fromCallable(() -> {
            List<Mail> mails = mailsFromServerGetter.get();
            if (accountManager.needLogin()) {
                return new ArrayList<Mail>();
            }
            for (Mail mail : mails) {
                mail.setMailType(mailType);
                List<T> mailsByIdList = mailsByIdGetter.apply(mail.getMessageUID());
                T mailEntity = constructor.createEntity(
                        mail.getMessageUID(),
                        mail.getAuthorEmail(),
                        mail.getAuthorName(),
                        mail.getRecipients().isEmpty() ? "" : mail.getRecipients().get(0),
                        mail.getTopic(),
                        mail.getText(),
                        mail.getDate(),
                        mail.isRead(),
                        mail.isInFavourites()
                );
                if (mailsByIdList.size() == 0) {
                    toDBInserter.accept(mailEntity);
                } else {
                    mailEntity.setRead(mailsByIdList.get(0).getRead());
                    mail.setIsRead(mailsByIdList.get(0).getRead());

                    mailEntity.setInFavourites(mailsByIdList.get(0).getInFavourites());
                    mail.setIsInFavourites(mailsByIdList.get(0).getInFavourites());

                    dbUpdater.accept(mailEntity);
                }
            }
            return mails;
        }).subscribeOn(Schedulers.io());
    }


    public Observable<List<Mail>> getMails(MailType mailType) {
        switch (mailType) {
            case INCOMING:
                return getIncomingMails();
            case SENT:
                return getSentMails();
            case DRAFT:
                return getDrafts();
            case SPAM:
                return getSpam();
            default:
                return Observable.error(new Exception("Invalid mail type"));
        }
    }

    private <T extends MailEntity> Observable<List<Mail>> getMails(Supplier<List<T>> mailsFromDBGetter,
                                                                   Supplier<List<Mail>> mailsFromServerGetter,
                                                                   LongFunction<List<T>> mailsByIdGetter,
                                                                   Consumer<T> toDBInserter,
                                                                   Consumer<T> dbUpdater,
                                                                   MailEntityConstructorInterface<T> constructor,
                                                                   MailType mailType) {
        return Observable.merge(
                        Observable.fromSingle(getLocalMails(mailsFromDBGetter)),
                        Observable.fromSingle(getMailsFromServer(
                                mailsFromServerGetter, mailsByIdGetter, toDBInserter, dbUpdater, constructor, mailType))
                )
                .take(2)
                .subscribeOn(Schedulers.io());
    }


    public Observable<List<Mail>> getIncomingMails() {
        return getMails(mailDao::getAllIncomingMails,
                mailServerConnector::getIncomingMails,
                mailDao::getIncomingMailById,
                mailDao::insertIncomingMail,
                mailDao::updateIncomingMail,
                IncomingMail::new,
                MailType.INCOMING);

        /*return Observable
                /*.interval(0, 1, TimeUnit.MINUTES)
                .timeInterval()
                .map(i -> {
                    return mailServerConnector.getIncomingMails();
                })*
                .fromCallable(() -> {
                    /*List<Mail> mails = mailServerConnector.getIncomingMails();
                    Log.i("", "---------------------mails number:" + mails);
                    return mails;*
                })
                .subscribeOn(Schedulers.io());*/
    }

    public Observable<List<Mail>> getSentMails() {
        return getMails(mailDao::getAllSentMails,
                mailServerConnector::getSentMails,
                mailDao::getSentMailById,
                mailDao::insertSentMail,
                mailDao::updateSentMail,
                SentMail::new,
                MailType.SENT);
    }

    public Observable<List<Mail>> getDrafts() {
        return getMails(mailDao::getAllDraftMails,
                mailServerConnector::getDrafts,
                mailDao::getDraftMailById,
                mailDao::insertDraftMail,
                mailDao::updateDraftMail,
                Draft::new,
                MailType.DRAFT);
    }

    /*private <T extends MailEntity> Mail getMailByUID(LongFunction<List<T>> mailsByIdGetter, int uid, MailType mailType) {

        List<T> mailEntitiesFromDB = mailsByIdGetter.apply(uid);
        if (mailEntitiesFromDB.size() == 0) {
            return null;
        }
        MailEntity mailEntity = mailEntitiesFromDB.get(0);

        Mail mail = new Mail();
        mail.setMessageUID(mailEntity.getMessageUID());
        mail.setAuthorEmail(mailEntity.getAuthorEmail());
        mail.setAuthorName(mailEntity.getAuthorName());
        mail.addRecipient(((MailEntity) mailEntity).getRecipientEmail());
        mail.setTopic(mailEntity.getTopic());
        mail.setText(mailEntity.getContent());
        mail.setDate(mailEntity.getDate());
        mail.setIsRead(mailEntity.getRead());
        mail.setIsInFavourites(mailEntity.getInFavourites());
        mail.setMailType(MailType.valueOf(mailEntity.getMessageType()));
        return mail;
    }*/

    public Observable<List<Mail>> getFavourites() {
        return Observable.fromCallable(() -> {
            List<? extends MailEntity> favIncoming = mailDao.getFavouriteIncomingMails();
            List<? extends MailEntity> favSent = mailDao.getFavouriteSentMails();
            List<? extends MailEntity> favDraft = mailDao.getFavouriteDraftMails();
            List<? extends MailEntity> favSpam = mailDao.getFavouriteSpamMails();
            List<? extends MailEntity> favDeferred = mailDao.getFavouriteDeferredMails();
//            List<? extends MailEntity> favouriteMailsFromDB = Stream.concat(
//                            Stream.concat(favIncoming.stream(), favSent.stream()), Stream.concat(favDraft.stream(), favSpam.stream()))
//                    .collect(Collectors.toList());
            Stream<? extends MailEntity> stream = Stream.concat(favIncoming.stream(), favSent.stream());
            stream = Stream.concat(stream, favDraft.stream());
            stream = Stream.concat(stream, favSpam.stream());
            stream = Stream.concat(stream, favDeferred.stream());
            List<? extends MailEntity> favouriteMailsFromDB = stream.collect(Collectors.toList());
            List<Mail> favouriteMails = new ArrayList<>(favouriteMailsFromDB.size());
            for (MailEntity mailEntity : favouriteMailsFromDB) {
                Mail mail = mailEntityToMail(mailEntity);
                favouriteMails.add(mail);
            }
            return favouriteMails;
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Mail>> getSpam() {
        return getMails(mailDao::getAllSpamMails,
                mailServerConnector::getSpam,
                mailDao::getSpamMailById,
                mailDao::insertSpamMail,
                mailDao::updateSpamMail,
                SpamMail::new,
                MailType.SPAM);
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
        return Completable.fromCallable(() -> {
            if (mailDao.getDraftMailById(mail.getMessageUID()).isEmpty()) {
                System.out.println("------- in new");
                // save new draft
                MailServerConnector.DraftInfo draftInfo = mailServerConnector.saveDraft(mail);
                Mail draftMail = draftInfo.getDraftMail();
                if (draftInfo.getDraftMail().getMessageUID() == -1) {
                    throw new Exception("Error while saving draft");
                } else {
                    // save draft locally
                    Draft draft = mailToDraft(draftMail);
                    mailDao.insertDraftMail(draft);
                }
            } else {
                System.out.println("------- in update");
                // update existing draft
                Completable.mergeArray(Completable.fromCallable(() -> {
                            if (mailServerConnector.updateDraft(mail)) {
                                return Completable.complete();
                            } else {
                                throw new Exception("Error while saving draft");
                            }
                        }), Completable.fromCallable(() -> {
                            System.out.println("----- in local update before update");
                            mailDao.updateDraftMail(mailToDraft(mail));
                            System.out.println("----- in local update after update");
                            return Completable.complete();
                        }))
                        .subscribe();
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
                    MailEntity mailEntity = null;
                    switch (mail.getMailType()) {
                        case INCOMING:
                            List<IncomingMail> incomingMails = mailDao.getIncomingMailById(mail.getMessageUID());
                            if (!incomingMails.isEmpty()) {
                                mailEntity = incomingMails.get(0);
                                mailEntity.setInFavourites(mail.isInFavourites());
                                mailDao.updateIncomingMail((IncomingMail) mailEntity);
                            }
                            break;
                        case SENT:
                            List<SentMail> sentMails = mailDao.getSentMailById(mail.getMessageUID());
                            if (!sentMails.isEmpty()) {
                                mailEntity = sentMails.get(0);
                                mailEntity.setInFavourites(mail.isInFavourites());
                                mailDao.updateSentMail((SentMail) mailEntity);
                            }
                            break;
                        case DRAFT:
                            List<Draft> draftMails = mailDao.getDraftMailById(mail.getMessageUID());
                            if (!draftMails.isEmpty()) {
                                mailEntity = draftMails.get(0);
                                mailEntity.setInFavourites(mail.isInFavourites());
                                mailDao.updateDraftMail((Draft) mailEntity);
                            }
                            break;
                        case SPAM:
                            List<SpamMail> spamMails = mailDao.getSpamMailById(mail.getMessageUID());
                            if (!spamMails.isEmpty()) {
                                mailEntity = spamMails.get(0);
                                mailEntity.setInFavourites(mail.isInFavourites());
                                mailDao.updateSpamMail((SpamMail) mailEntity);
                            }
                            break;
                        case DEFERRED:
                            List<DeferredMail> deferredMails = mailDao.getDeferredMailByRequestCode((int) mail.getMessageUID());
                            if (!deferredMails.isEmpty()) {
                                mailEntity = deferredMails.get(0);
                                mailEntity.setInFavourites(mail.isInFavourites());
                                mailDao.updateDeferredMail((DeferredMail) mailEntity);
                            }
                            break;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private <T extends MailEntity> Single<Mail> getMailByID(long messageUID, LongFunction<List<T>> mailsByIdGetter) {
        return Single.fromCallable(() -> {
                    List<T> mailEntityList = mailsByIdGetter.apply(messageUID);
                    if (mailEntityList.size() == 0) {
                        return null;
                    } else {
                        T mailEntity = mailEntityList.get(0);
                        return mailEntityToMail(mailEntity);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Single<Mail> getIncomingMailById(long messageUID) {
        return getMailByID(messageUID, mailDao::getIncomingMailById);
    }

    public Single<Mail> getSentMailById(long messageUID) {
        return getMailByID(messageUID, mailDao::getSentMailById);
    }

    public Single<Mail> getDraftMailById(long messageUID) {
        return getMailByID(messageUID, mailDao::getDraftMailById);
    }

//    public Single<Mail> getFavouriteMailById(long messageUID) {
//        return getMailByID(messageUID, mailDao::getFavouriteMailById);
//    }

    public Single<Mail> getSpamMailById(long messageUID) {
        return getMailByID(messageUID, mailDao::getSpamMailById);
    }

    public Single<Mail> getTrashMailById(long messageUID) {
        return getMailByID(messageUID, mailDao::getTrashMailById);
    }

    public Mail getDeferredMailByRequestCodeSync(int requestCode) {
        List<DeferredMail> mailEntityList = mailDao.getDeferredMailByRequestCode(requestCode);
        if (mailEntityList.size() == 0) {
            return null;
        } else {
            DeferredMail deferredMail = mailEntityList.get(0);
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
                            MailEntity mailEntity;
                            switch (mail.getMailType()) {
                                case INCOMING:
                                    List<IncomingMail> incomingMails = mailDao.getIncomingMailById(mail.getMessageUID());
                                    if (incomingMails.size() > 0) {
                                        mailEntity = incomingMails.get(0);
                                        mailEntity.setRead(mail.isRead());
                                        mailDao.updateIncomingMail((IncomingMail) mailEntity);
                                    }
                                    break;
                                case SENT:
                                    List<SentMail> sentMails = mailDao.getSentMailById(mail.getMessageUID());
                                    if (sentMails.size() > 0) {
                                        mailEntity = sentMails.get(0);
                                        mailEntity.setRead(mail.isRead());
                                        mailDao.updateSentMail((SentMail) mailEntity);
                                    }
                                    break;
//                                case FAVOURITE:
//                                    mailEntity = mailDao.getFavouriteMailById(mail.getMessageUID()).get(0);
//                                    mailEntity.setRead(mail.isRead());
//                                    mailDao.updateFavouriteMail((FavouriteMail) mailEntity);
//                                    break;
                                case DRAFT:
                                    List<Draft> draftMails = mailDao.getDraftMailById(mail.getMessageUID());
                                    if (draftMails.size() > 0) {
                                        mailEntity = draftMails.get(0);
                                        mailEntity.setRead(mail.isRead());
                                        mailDao.updateDraftMail((Draft) mailEntity);
                                    }
                                    break;
                                case SPAM:
                                    List<SpamMail> spamMails = mailDao.getSpamMailById(mail.getMessageUID());
                                    if (spamMails.size() > 0) {
                                        mailEntity = spamMails.get(0);
                                        mailEntity.setRead(mail.isRead());
                                        mailDao.updateSpamMail((SpamMail) mailEntity);
                                    }
                                    break;
                            }
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void addDeferredMailToDB(Mail mail, int requestCode) {
        Completable.fromRunnable(() -> {
                    DeferredMail deferredMail = new DeferredMail(
                            mail.getAuthorEmail(),
                            mail.getAuthorName(),
                            mail.getRecipients().isEmpty() ? "" : mail.getRecipients().get(0),
                            mail.getTopic(),
                            mail.getText(),
                            mail.getDate(),
                            true,
                            mail.isInFavourites(),
                            requestCode
                    );
                    mailDao.insertDeferredMail(deferredMail);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteDeferredMailSync(int requestCode) {
        List<DeferredMail> deferredMailList = mailDao.getDeferredMailByRequestCode(requestCode);
        if (deferredMailList.isEmpty()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DeferredSendBroadcastReceiver.class);
        intent.setAction(WriteMessageActivity.DEFERRED_SEND_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, deferredMailList.get(0).getRequestCode(), intent, PendingIntent.FLAG_IMMUTABLE | Intent.FILL_IN_DATA | PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        mailDao.deleteDeferredMail(deferredMailList.get(0));
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

    private <T> Completable deleteLocalMail(long mailUid, LongFunction<List<T>> mailByUidGetter, Consumer<T> mailFromDBDeleter) {
        return Completable.fromRunnable(() -> {
            List<T> mailEntityList = mailByUidGetter.apply(mailUid);
            if (mailEntityList.isEmpty()) {
                return;
            }
            mailFromDBDeleter.accept(mailEntityList.get(0));
        }).subscribeOn(Schedulers.io());
    }

    private Completable deleteLocalMail(Mail mail) {
        switch (mail.getMailType()) {
            case INCOMING:
                return deleteLocalMail(mail.getMessageUID(), mailDao::getIncomingMailById, mailDao::deleteIncomingMail);
            case SENT:
                return deleteLocalMail(mail.getMessageUID(), mailDao::getSentMailById, mailDao::deleteSentMail);
            case DRAFT:
                return deleteLocalMail(mail.getMessageUID(), mailDao::getDraftMailById, mailDao::deleteDraftMail);
            case SPAM:
                return deleteLocalMail(mail.getMessageUID(), mailDao::getSpamMailById, mailDao::deleteSpamMail);
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
        mail.setMessageUID(mailEntity.getMessageUID());
        mail.setAuthorEmail(mailEntity.getAuthorEmail());
        mail.setAuthorName(mailEntity.getAuthorName());
        mail.addRecipient(mailEntity.getRecipientEmail());
        mail.setTopic(mailEntity.getTopic());
        mail.setText(mailEntity.getContent());
        mail.setDate(mailEntity.getDate());
        mail.setIsRead(mailEntity.getRead());
        mail.setIsInFavourites(mailEntity.getInFavourites());
        mail.setMailType(MailType.valueOf(mailEntity.getMessageType()));
        return mail;
    }


    private Draft mailToDraft(Mail mail) {
        return new Draft(mail.getMessageUID(),
                mail.getAuthorEmail(),
                mail.getAuthorName(),
                mail.getRecipients().isEmpty() ? "" : mail.getRecipients().get(0),
                mail.getTopic(),
                mail.getText(),
                mail.getDate(),
                mail.isRead(),
                mail.isInFavourites());
    }
}
