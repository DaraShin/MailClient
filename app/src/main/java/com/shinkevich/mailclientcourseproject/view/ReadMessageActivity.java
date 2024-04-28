package com.shinkevich.mailclientcourseproject.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shinkevich.mailclientcourseproject.model.Mail;
import com.shinkevich.mailclientcourseproject.model.MailType;
import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.viewmodel.ReadMessageViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class ReadMessageActivity extends AppCompatActivity {
    public static String mailExtraKey = "mail";

    private TextView topicTV;
    private TextView dateTV;
    private TextView senderEmailTV;
    private TextView receiverEmailTV;
    private TextView mailTextTV;
    private TextView categoryTV;
    private ImageButton favouritesBtn;
    private FloatingActionButton backBtn;
    private FloatingActionButton deleteMailBtn;
    private FloatingActionButton editDraftBtn;

    private Mail mail;

    private ReadMessageViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_message_activity);

        viewModel = new ViewModelProvider(this).get(ReadMessageViewModel.class);

        getViews();
        setEventHandlers();
        mailTextTV.setMovementMethod(new ScrollingMovementMethod());

        Bundle args = getIntent().getExtras();
        mail = (Mail) args.getSerializable(mailExtraKey);
        if (mail.getMailType().equals(MailType.DRAFT)) {
            editDraftBtn.setVisibility(View.VISIBLE);
        }

        configViews(mail);
    }

    private void getViews() {
        topicTV = (TextView) findViewById(R.id.topicTextViewRead);
        dateTV = (TextView) findViewById(R.id.dateTextViewRead);
        senderEmailTV = (TextView) findViewById(R.id.fromEmailTextView);
        receiverEmailTV = (TextView) findViewById(R.id.toEmailTextView);
        mailTextTV = (TextView) findViewById(R.id.messageTextViewRead);
        categoryTV = (TextView) findViewById(R.id.categoryTV);
        favouritesBtn = (ImageButton) findViewById(R.id.favouritesBtnRead);
        backBtn = (FloatingActionButton) findViewById(R.id.backBtnRead);
        deleteMailBtn = (FloatingActionButton) findViewById(R.id.deleteMailBtn);
        editDraftBtn = (FloatingActionButton) findViewById(R.id.editDraftBtn);
    }

    private void configViews(Mail message) {
        message.setIsRead(true);

        if (message.getTopic() == null || message.getTopic().trim().equals("")) {
            topicTV.setText(getString(R.string.without_topic));
        } else {
            topicTV.setText(message.getTopic());
        }

        dateTV.setText(CommonViewServices.getDateToShow(message.getDate()));
        senderEmailTV.setText(message.getAuthorEmail());
        mailTextTV.setText(message.getText());

        if (message.getRecipients() != null && !message.getRecipients().isEmpty()) {
            receiverEmailTV.setText(message.getRecipients().get(0));
        }

        switch (mail.getMailType()) {
            case INCOMING:
                categoryTV.setText(getString(R.string.incoming));
                break;
            case SENT:
                categoryTV.setText(getString(R.string.sent));
                break;
            case DRAFT:
                categoryTV.setText(getString(R.string.drafts));
                break;
            case FAVOURITE:
                break;
            case SPAM:
                categoryTV.setText(getString(R.string.spam));
                break;
            case DEFERRED:
                categoryTV.setText(getString(R.string.deffered));
                break;
        }

        if (message.isInFavourites()) {
            ((ImageButton) favouritesBtn).setImageDrawable(getDrawable(R.drawable.selected_star_icon));
        } else {
            ((ImageButton) favouritesBtn).setImageDrawable(getDrawable(R.drawable.star_icon));
        }
    }

    private void setEventHandlers() {
        backBtn.setOnClickListener(backClickListener);
        favouritesBtn.setOnClickListener(favouritesBtnClickListener);
        deleteMailBtn.setOnClickListener(deleteMailBtnClickListener);
        editDraftBtn.setOnClickListener(editDraftBtnClickListener);
    }

    private View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ReadMessageActivity.this.onBackPressed();
        }
    };

    private View.OnClickListener favouritesBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View button) {
            viewModel.favouriteClick(mail);
            mail.setIsInFavourites(!mail.isInFavourites());
            if (mail.isInFavourites()) {
                ((ImageButton) button).setImageDrawable(getDrawable(R.drawable.selected_star_icon));
            } else {
                ((ImageButton) button).setImageDrawable(getDrawable(R.drawable.star_icon));
            }
        }
    };

    private View.OnClickListener deleteMailBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View button) {
            ProgressDialog[] progressDialog = new ProgressDialog[1];
            viewModel.deleteMail(mail)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> {
                        progressDialog[0] = ProgressDialog.show(
                                ReadMessageActivity.this, "", getString(R.string.deleting_mail));
                    })
                    .subscribe(() -> {
                        if (progressDialog[0].isShowing()) {
                            progressDialog[0].dismiss();
                        }
                        Toast.makeText(getApplicationContext(), R.string.mail_deleted, Toast.LENGTH_SHORT).show();
                        backBtn.callOnClick();
                    }, (exception) -> {
                        if (progressDialog[0].isShowing()) {
                            progressDialog[0].dismiss();
                        }
                        Toast.makeText(getApplicationContext(), R.string.mail_deleting_error, Toast.LENGTH_SHORT).show();
                        backBtn.callOnClick();
                    });
//            viewModel.deleteMail(mail);
//            backBtn.callOnClick();
        }
    };

    private View.OnClickListener editDraftBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent editMessageIntent = new Intent(ReadMessageActivity.this, WriteMessageActivity.class);
            editMessageIntent.putExtra(WriteMessageActivity.MAIL_TO_EDIT_EXTRA, mail);
            finish();
            startActivity(editMessageIntent);
        }
    };
}
