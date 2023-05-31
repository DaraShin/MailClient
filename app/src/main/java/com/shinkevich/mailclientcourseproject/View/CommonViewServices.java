package com.shinkevich.mailclientcourseproject.View;

import static kotlin.random.RandomKt.Random;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.shinkevich.mailclientcourseproject.Model.Mail;
import com.shinkevich.mailclientcourseproject.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class CommonViewServices {
    protected static final SimpleDateFormat baseFormatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    private static String sendMailChannelId = "sendMailChannelId";
    private static String sendMailChannelName = "sendMailChannel";

    public static String getDateToShow(String fullDateString) {
        if (fullDateString == null) {
            return "";
        }
        Date currentDate = new Date();
        Date date;
        try {
            date = baseFormatter.parse(fullDateString);
            if (dateFormatter.format(currentDate).equals(dateFormatter.format(date))) {
                return timeFormatter.format(date);
            } else {
                return dateFormatter.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void sortMessagesByDate(ArrayList<Mail> messagesList) {
        Collections.sort(messagesList, new Comparator<Mail>() {
            @Override
            public int compare(Mail mail1, Mail mail2) {
                try {
                    return -1 * baseFormatter.parse(mail1.getDate()).compareTo(baseFormatter.parse(mail2.getDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        });
    }

    public static void showNotification(Context context, String title, String text, Mail mail) {
        RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.notification_layout);
        remoteViews.setTextViewText(R.id.authorTextViewNotification, mail.getAuthorEmail());

        if (mail.getTopic() == null || mail.getTopic().trim().isEmpty()) {
            remoteViews.setViewVisibility(R.id.topicTextViewNotification, View.GONE);
        } else {
            remoteViews.setTextViewText(R.id.topicTextViewNotification, mail.getTopic());
        }
        remoteViews.setTextViewText(R.id.textTextViewNotification, mail.getText());
        //remoteViews.setOnClickPendingIntent(R.id.root, rootPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(sendMailChannelId, sendMailChannelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, sendMailChannelId)
                .setSmallIcon(R.drawable.notification_icon)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL);
//                .setContentTitle("gfvfgbf")
//                .setContentText("cfvcvc");


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Random(System.currentTimeMillis()).nextInt(), builder.build());
    }
}
