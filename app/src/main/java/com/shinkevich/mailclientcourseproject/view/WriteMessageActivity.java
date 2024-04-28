package com.shinkevich.mailclientcourseproject.view;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shinkevich.mailclientcourseproject.model.AccountManager;
import com.shinkevich.mailclientcourseproject.model.DeferredSendBroadcastReceiver;
import com.shinkevich.mailclientcourseproject.model.Mail;
import com.shinkevich.mailclientcourseproject.model.MailType;
import com.shinkevich.mailclientcourseproject.model.Repository;
import com.shinkevich.mailclientcourseproject.R;

import java.util.Calendar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;

public class WriteMessageActivity extends AppCompatActivity {
    public static final String MAIL_TO_DEFER_EXTRA = "mailToSend";
    public static final String REQUEST_CODE_EXTRA = "delayRequestCode";
    public static final String MAIL_TO_EDIT_EXTRA = "delayRequestCode";
    public static final String DEFERRED_SEND_ACTION = "com.shinkevich.mailclientcourseproject.SEND_MAIL";

    private EditText fromMailET;
    private EditText toMailET;
    private EditText topicET;
    private EditText textET;
    private FloatingActionButton sendBtn;
    private FloatingActionButton backBtn;
    private FloatingActionButton delayBtn;
    private FloatingActionButton attachBtn;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    private Calendar sendMailCalendar;

    private Repository repository;

    private boolean isDraft = false;
    private long draftUid = 0;
    private String draftId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_message_activity);

        getViews();
        setEventHandlers();

        fromMailET.setText(new AccountManager(getApplicationContext()).getActiveUser().getEmail());
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey(MAIL_TO_EDIT_EXTRA)) {
            Mail draftExtra = (Mail) args.getSerializable(MAIL_TO_EDIT_EXTRA);
            isDraft = true;
            draftUid = draftExtra.getMessageUID();
            draftId = draftExtra.getMailID();
            configView(draftExtra);
        }

        repository = new Repository(this);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void configView(Mail mail) {
        if (!mail.getRecipients().isEmpty()) {
            toMailET.setText(mail.getRecipients().get(0));
        }
        topicET.setText(mail.getTopic());
        textET.setText(mail.getText());
    }

    private void getViews() {
        fromMailET = (EditText) findViewById(R.id.fromMailEdTxt);
        toMailET = (EditText) findViewById(R.id.toMailEdTxt);
        topicET = (EditText) findViewById(R.id.topicEdTxt);
        textET = (EditText) findViewById(R.id.messageTextViewRead);
        sendBtn = (FloatingActionButton) findViewById(R.id.sendBtn);
        backBtn = (FloatingActionButton) findViewById(R.id.backBtn);
        delayBtn = (FloatingActionButton) findViewById(R.id.delayBtn);
        attachBtn = (FloatingActionButton) findViewById(R.id.attachmentBtn);
    }

    private void setEventHandlers() {
        sendBtn.setOnClickListener(sendBtnClickListener);
        backBtn.setOnClickListener(backBtnClickListener);
        delayBtn.setOnClickListener(delayBtnClickListener);
        attachBtn.setOnClickListener(attachBtnClickListener);
    }

    private boolean checkRecipientIsSet(String recipient) {
        if (recipient.equals("")) {
            new AlertDialog.Builder(WriteMessageActivity.this)
                    .setMessage(getString(R.string.add_receiver))
                    .setPositiveButton((CharSequence) getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    private Mail getMailFromActivity() {
        Mail mailToSend = new Mail();
        mailToSend.setTopic(topicET.getText().toString());
        mailToSend.setText(textET.getText().toString());
        mailToSend.addRecipient(toMailET.getText().toString());
        mailToSend.setAuthorEmail(fromMailET.getText().toString());
        return mailToSend;
    }

    private View.OnClickListener sendBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Mail mailToSend = getMailFromActivity();
            String to = mailToSend.getRecipients().get(0);
            if (checkRecipientIsSet(to)) {
                //ServerConnector.sendMessage(to, topic, text, getApplicationContext());
                ProgressDialog[] progressDialog = new ProgressDialog[1];
                if (isDraft) {
                    Mail draft = getMailFromActivity();
                    draft.setMailID(draftId);
                    draft.setMessageUID(draftUid);
                    draft.setMailType(MailType.DRAFT);
                    repository.deleteMail(draft).subscribe();
                }
                repository.sendMessage(mailToSend, to)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> {
                            progressDialog[0] = ProgressDialog.show(
                                    WriteMessageActivity.this, "", getString(R.string.sending_message));
                        })
                        .subscribe(isSentSuccessfully -> {
                            if (progressDialog[0].isShowing()) {
                                progressDialog[0].dismiss();
                            }
                            if (isSentSuccessfully) {
                                Toast.makeText(getApplicationContext(), R.string.message_sent, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.send_error, Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        });
            }
        }
    };

    private View.OnClickListener backBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            WriteMessageActivity.this.onBackPressed();
        }
    };

    private View.OnClickListener delayBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            datePickerDialog = new DatePickerDialog(
                    WriteMessageActivity.this,
                    dataSetListener,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
    };

    private DatePickerDialog.OnDateSetListener dataSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            sendMailCalendar = Calendar.getInstance();
            sendMailCalendar.set(year, month, day);
            timePickerDialog = new TimePickerDialog(
                    WriteMessageActivity.this,
                    timeSetListener,
                    (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24,
                    Calendar.getInstance().get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
            sendMailCalendar.set(Calendar.HOUR_OF_DAY, hour);
            sendMailCalendar.set(Calendar.MINUTE, minute);
            setDeferredSend();
        }
    };

    private void setDeferredSend() {
        Mail mailToSend = getMailFromActivity();
        String recipient = mailToSend.getRecipients().get(0);
        if (!checkRecipientIsSet(recipient)) {
            return;
        }
        int currentTime = (int) Calendar.getInstance().getTimeInMillis();

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(WriteMessageActivity.this, DeferredSendBroadcastReceiver.class);
        intent.setAction(DEFERRED_SEND_ACTION);
        intent.putExtra(REQUEST_CODE_EXTRA, currentTime);

        PendingIntent pendingIntent = null;
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), currentTime, intent, PendingIntent.FLAG_IMMUTABLE | Intent.FILL_IN_DATA);
        /*} else {
            pendingIntent = PendingIntent.getBroadcast(WriteMessageActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }*/
        //alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pendingIntent);


        //alarmMgr.set(AlarmManager.RTC_WAKEUP, sendMailCalendar.getTimeInMillis(), pendingIntent);
        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, sendMailCalendar.getTimeInMillis(), pendingIntent);

        String dateString = CommonViewServices.baseFormatter.format(sendMailCalendar.getTime());
        mailToSend.setDate(dateString);
        repository.addDeferredMailToDB(mailToSend, currentTime);
        if (isDraft) {
            //mailToSend.setMessageUID(draftUid);
            mailToSend.setMailType(MailType.DRAFT);
            repository.deleteMail(mailToSend).subscribe();
        }
        Toast.makeText(getApplicationContext(), R.string.message_deferred, Toast.LENGTH_SHORT).show();
        finish();
    }

    private View.OnClickListener attachBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // выбор файла на устройстве
        }
    };

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.save_draft))
                .setPositiveButton((CharSequence) getString(R.string.yes), (DialogInterface dialogInterface, int i) -> {
                    Mail draft = getMailFromActivity();
                    if (isDraft) {
                        String dateString = CommonViewServices.baseFormatter.format(System.currentTimeMillis());
                        draft.setDate(dateString);
                        //draft.setMessageUID(draftUid);
                        draft.setMailType(MailType.DRAFT);
                    }

                    ProgressDialog[] progressDialog = new ProgressDialog[1];
                    repository.saveDraft(draft)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {
                                    progressDialog[0] = ProgressDialog.show(
                                            WriteMessageActivity.this, "", getString(R.string.saving_draft));
                                }
                                @Override
                                public void onComplete() {
                                    if (progressDialog[0].isShowing()) {
                                        progressDialog[0].dismiss();
                                    }
                                    Toast.makeText(getApplicationContext(), R.string.draft_saved, Toast.LENGTH_SHORT).show();
                                    WriteMessageActivity.super.onBackPressed();
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    if (progressDialog[0].isShowing()) {
                                        progressDialog[0].dismiss();
                                    }
                                    Toast.makeText(getApplicationContext(), R.string.draft_saving_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton(getString(R.string.no), (DialogInterface dialogInterface, int i) -> {
                    super.onBackPressed();
                })
                .show();
    }
}