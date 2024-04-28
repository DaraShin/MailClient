package com.shinkevich.mailclientcourseproject;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.shinkevich.mailclientcourseproject.model.AccountManager;
import com.shinkevich.mailclientcourseproject.model.database.entity.MailEntity;
import com.shinkevich.mailclientcourseproject.model.database.MailDao;
import com.shinkevich.mailclientcourseproject.model.database.MailsDatabase;
import com.shinkevich.mailclientcourseproject.model.MailServiceEnum;
import com.shinkevich.mailclientcourseproject.model.MailType;
import com.shinkevich.mailclientcourseproject.model.Repository;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

@RunWith(AndroidJUnit4.class)
public class PerformanceTests {
    private static final Integer ITERATIONS_TO_DROP = 10;
    private static final Integer ITERATIONS_TO_MEASURE = 100;
    private static final Double NS_TO_MS_MULTIPLIER = 0.000001;
    private static final String TAG = PerformanceTests.class.getSimpleName();

    private MailDao mailDao = MailsDatabase.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext()).mailDao();

    @Test
    public void getDraftsFromDBTest() {
        AccountManager accountManager = new AccountManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
        accountManager.setActiveUser("email", "password", MailServiceEnum.GMAIL);
        Repository repository = new Repository(InstrumentationRegistry.getInstrumentation().getTargetContext());
        long totalTime = 0;
        for (int i = 1; i <= (ITERATIONS_TO_DROP + ITERATIONS_TO_MEASURE); i++) {
            long startTime = System.nanoTime();
            repository.getLocalMails(MailType.DRAFT)
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe();
            long iterationTime = System.nanoTime() - startTime;
            Log.d(TAG, "iteration " + i + ": " + iterationTime + " ns");
            if (i > ITERATIONS_TO_DROP) {
                totalTime += iterationTime;
            }
        }
        accountManager.logout();
        Log.d(TAG, "getDraftsFromDBTest: " + ((double) totalTime / ITERATIONS_TO_MEASURE * NS_TO_MS_MULTIPLIER) + " ms");
    }

    @Test
    public void getDraftsFromServerTest() {
        AccountManager accountManager = new AccountManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
        accountManager.setActiveUser("email", "password", MailServiceEnum.GMAIL);
        Repository repository = new Repository(InstrumentationRegistry.getInstrumentation().getTargetContext());
        long totalTime = 0;
        for (int i = 1; i <= (ITERATIONS_TO_DROP + ITERATIONS_TO_MEASURE); i++) {
            long startTime = System.nanoTime();
            repository.getMailsFromServer(MailType.DRAFT)
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe();
            long iterationTime = System.nanoTime() - startTime;
            Log.d(TAG, "iteration " + i + ": " + iterationTime + " ns");
            if (i > ITERATIONS_TO_DROP) {
                totalTime += iterationTime;
            }
        }
        accountManager.logout();
        Log.d(TAG, "getDraftsFromServerTest: " + ((double) totalTime / ITERATIONS_TO_MEASURE * NS_TO_MS_MULTIPLIER) + " ms");
    }

    @Test
    public void authorizationTest() {
        Repository repository = new Repository(InstrumentationRegistry.getInstrumentation().getTargetContext());
        long totalTime = 0;
        for (int i = 1; i <= (ITERATIONS_TO_DROP + ITERATIONS_TO_MEASURE); i++) {
            long startTime = System.nanoTime();
            repository.checkAuthenticationData("email", "password")
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe(res -> Log.d(TAG, "authorized: " + res));
            long iterationTime = System.nanoTime() - startTime;
            Log.d(TAG, "iteration " + i + ": " + iterationTime + " ns");
            if (i > ITERATIONS_TO_DROP) {
                totalTime += iterationTime;
            }
        }
        Log.d(TAG, "authorizationTest: " + ((double) totalTime / ITERATIONS_TO_MEASURE * NS_TO_MS_MULTIPLIER) + " ms");
    }

    @Test
    public void clearDBTest() throws ExecutionException, InterruptedException {
        Repository repository = new Repository(InstrumentationRegistry.getInstrumentation().getTargetContext());
        long totalTime = 0;
        repository.clearDBForTest().get();
        for (int i = 1; i <= (ITERATIONS_TO_DROP + ITERATIONS_TO_MEASURE); i++) {
            insertTestData();
            long startTime = System.nanoTime();
            repository.clearDBForTest().get();
            long iterationTime = System.nanoTime() - startTime;
            Log.d(TAG, "iteration " + i + ": " + iterationTime + " ns");
            if (i > ITERATIONS_TO_DROP) {
                totalTime += iterationTime;
            }
        }
        Log.d(TAG, "clearDBTest: " + ((double) totalTime / ITERATIONS_TO_MEASURE * NS_TO_MS_MULTIPLIER) + " ms");
    }

    private void insertTestData() {
        for (int i = 0; i < 100; i++) {
            mailDao.insertMail(new MailEntity(
                    String.valueOf(i),
                    i,
                    "Test message author email",
                    "Test message author name",
                    "Test message recipient",
                    "Test message topic",
                    "Test message text",
                    "Test message date",
                    true,
                    false,
                    MailType.DRAFT
            ));
        }
    }

    @Test
    public void getMailByIdTest() {
        AccountManager accountManager = new AccountManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
        accountManager.setActiveUser("email", "password", MailServiceEnum.GMAIL);
        Repository repository = new Repository(InstrumentationRegistry.getInstrumentation().getTargetContext());
        long totalTime = 0;
        repository.getMailsFromServer(MailType.DRAFT).blockingSubscribe();
        for (int i = 1; i <= (ITERATIONS_TO_DROP + ITERATIONS_TO_MEASURE); i++) {
            insertTestDraft();
            long startTime = System.nanoTime();
            final int counter = 1;
            repository.getDraftMailByIdForTest("").take(1).observeOn(AndroidSchedulers.mainThread()).blockingSubscribe();
            long iterationTime = System.nanoTime() - startTime;
            Log.d(TAG, "iteration " + i + ": " + iterationTime + " ns");
            if (i > ITERATIONS_TO_DROP) {
                totalTime += iterationTime;
            }
            deleteTestDraft();
        }
        Log.d(TAG, "getMailByIdTest: " + ((double) totalTime / ITERATIONS_TO_MEASURE * NS_TO_MS_MULTIPLIER) + " ms");
    }

    private void insertTestDraft() {
        mailDao.insertMail(new MailEntity("0",
                0,
                "Test message author email",
                "Test message author name",
                "Test message recipient",
                "Test message topic",
                "Test message text",
                "Test message date",
                true,
                false,
                MailType.DRAFT
        ));
    }

    private void deleteTestDraft() {
        mailDao.deleteMail(mailDao.getMailByPK("0",MailType.DRAFT).get(0));
    }
}
