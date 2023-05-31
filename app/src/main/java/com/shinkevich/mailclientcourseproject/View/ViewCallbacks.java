package com.shinkevich.mailclientcourseproject.View;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.shinkevich.mailclientcourseproject.R;

/*public class ViewCallbacks {

    public static android.os.Handler handler = new Handler(Looper.getMainLooper());

    public static void showSentToast(Context context, Boolean successful) {
        Log.i("", "In toast method");
        if (successful) {
            Log.i("", "Showing toast");
            Log.i("", Thread.currentThread().getName());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, (CharSequence) context.getResources().getString(R.string.message_sent), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, (CharSequence) context.getResources().getString(R.string.send_error), Toast.LENGTH_LONG).show();
                }
            });

        }
    }
}*/
