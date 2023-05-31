package com.shinkevich.mailclientcourseproject.Model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.View.CommonViewServices;
import com.shinkevich.mailclientcourseproject.View.WriteMessageActivity;

public class SendMailWorker extends Worker {
    private Context context;

    public SendMailWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        int requestCode = getInputData().getInt(WriteMessageActivity.REQUEST_CODE_EXTRA, -1);
        Mail mailToSend = new Repository(context).getDeferredMailByRequestCodeSync(requestCode);

        try{
            new Repository(context).deleteDeferredMailSync(requestCode);
        }catch (Exception e){
            e.printStackTrace();
        }

        new MailServerConnector(context).sendMessage(mailToSend, mailToSend.getRecipients().get(0));

        CommonViewServices.showNotification(getApplicationContext(),
                getApplicationContext().getString(R.string.message_send_notification_title),
                "",
                mailToSend);
        return Result.success();
    }
}

/*public class SendMailWorker extends Worker {

    public static String progress = "progress";
    private static Integer NOTIFICATION_ID = 42;
    private static String CHANNEL_ID = "download_channel";
    private static String TITLE = "Download File";

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    public SendMailWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(TITLE)
                .setTicker(TITLE)
                .setOngoing(true)
                .setContentText("Download File Progress")
                .setSmallIcon(androidx.core.R.drawable.notification_icon_background);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        try {
            Integer start = getInputData().getInt("start", 0);
            Integer end = getInputData().getInt("end", 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel();
            }
            PendingIntent intent = WorkManager.getInstance(getApplicationContext()).createCancelPendingIntent(getId());
            notificationBuilder.addAction(new NotificationCompat.Action(android.R.drawable.ic_delete, "Cancel", intent));
            Notification notification = notificationBuilder.build();
            setForegroundAsync(new ForegroundInfo(NOTIFICATION_ID, notification));
            for (int i = start; i < end; i++){
                HashMap<String, Integer> workDataMap = new HashMap<>();
                workDataMap.put(progress, i);
                setProgressAsync(new Data(workDataMap));
                //setProgress(workDataOf(progress to i))
                showProgress(i);
                Thread.sleep(100L);
            }
            return Result.success();
        } catch (Exception ex){
            Log.e("Worker", ex.getMessage());
            return Result.failure();
        }
    }

    private void showProgress(Integer progress) {
        Notification notification = notificationBuilder
                .setProgress(100, progress, false)
                .build();
        ForegroundInfo foregroundInfo = new ForegroundInfo(NOTIFICATION_ID, notification);
        setForegroundAsync(foregroundInfo);
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, TITLE, importance);
        notificationManager.createNotificationChannel(channel);
    }


}*/
