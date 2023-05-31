package com.shinkevich.mailclientcourseproject.Model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.GsonBuilder;
import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.View.CommonViewServices;
import com.shinkevich.mailclientcourseproject.View.WriteMessageActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeferredSendBroadcastReceiver extends BroadcastReceiver {
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WriteMessageActivity.DEFERRED_SEND_ACTION.equals(action)) {
            /*executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Mail mailToSend = (Mail) intent.getExtras().getSerializable(WriteMessageActivity.MAIL_TO_DEFER_EXTRA);
                    int requestCode = intent.getIntExtra(WriteMessageActivity.REQUEST_CODE_EXTRA, -1);

                    if (hasInternetConnection(context)) {
                        new Repository(context).deleteDeferredMail(requestCode).subscribe();
                        new MailServerConnector(context).sendMessage(mailToSend, mailToSend.getRecipients().get(0));
                        CommonViewServices.showNotification(context,
                                context.getString(R.string.message_send_notification_title),
                                "",
                                mailToSend);
                    } else {
                        String mailToSendJson = new GsonBuilder().create().toJson(mailToSend);
                        Data requestData = new Data.Builder()
                                .putString(WriteMessageActivity.MAIL_TO_DEFER_EXTRA, mailToSendJson)
                                .putInt(WriteMessageActivity.REQUEST_CODE_EXTRA,requestCode)
                                .build();
                        Constraints constraints = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();

                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendMailWorker.class)
                                .setConstraints(constraints)
                                .setInputData(requestData)
                                .build();
                        WorkManager.getInstance(context).enqueue(workRequest);
                    }

                }
            });*/

            int requestCode = intent.getIntExtra(WriteMessageActivity.REQUEST_CODE_EXTRA, -1);
            Data requestData = new Data.Builder()
                    .putInt(WriteMessageActivity.REQUEST_CODE_EXTRA, requestCode)
                    .build();
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendMailWorker.class)
                    .setConstraints(constraints)
                    .setInputData(requestData)
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
//            }
        }
    }

    private boolean hasInternetConnection(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected();
    }
}
