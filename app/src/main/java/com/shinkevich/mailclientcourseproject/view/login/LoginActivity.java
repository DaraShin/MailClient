package com.shinkevich.mailclientcourseproject.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shinkevich.mailclientcourseproject.model.MailServiceEnum;
import com.shinkevich.mailclientcourseproject.model.AccountManager;
import com.shinkevich.mailclientcourseproject.model.Repository;
import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.view.maillist.ShowMailsListActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class LoginActivity extends AppCompatActivity {
    private Button enterBtn;
    private ImageButton mailRuImgBtn;
    private ImageButton gmailImgBtn;
    private ImageButton yandexRuImgBtn;
    //private EditText loginET;
    //private EditText passwordET;

    TextInputLayout loginLayout;
    TextInputEditText loginEdTxt;
    TextInputLayout passwordLayout;
    TextInputEditText passwordEdTxt;

    //private RecyclerView mailServicesRecyclerView;
    private LinearLayoutManager layoutManager;

    private AccountManager accountManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity2);

        accountManager = new AccountManager(getApplicationContext());

        if (!accountManager.needLogin()) {
            Intent intent = new Intent(LoginActivity.this, ShowMailsListActivity.class);
            startActivity(intent);
        }else{
            new Repository(getApplicationContext()).clearDB();
        }

        getViews();
        setEventHandlers();
        //configAdapter();


//        loginLayout.setError("Обязаельное поле");
//        loginLayout.setErrorEnabled(false);
//        passwordLayout.setError("Обязаельное поле");
//        passwordLayout.setErrorEnabled(false);
    }

    /*private boolean checkNeedLogin() {
        return !(sharedPreferences.contains("username") && sharedPreferences.contains("password"));
    }*/

    private void getViews() {
        enterBtn = (Button) findViewById(R.id.loginBtn);
        mailRuImgBtn = (ImageButton) findViewById(R.id.mailRuImgBtn);
        gmailImgBtn = (ImageButton) findViewById(R.id.gmailImgBtn);
        yandexRuImgBtn = (ImageButton) findViewById(R.id.yandexImgBtn);
        //loginET = (EditText) findViewById(R.id.loginEdTxt);
        //passwordET = (EditText) findViewById(R.id.passwordEdTxt);
        //passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        loginLayout = (TextInputLayout) findViewById(R.id.loginLayout);
        loginEdTxt = (TextInputEditText) findViewById(R.id.loginEdTxt);
        passwordLayout = (TextInputLayout) findViewById(R.id.passwordLayout);
        passwordEdTxt = (TextInputEditText) findViewById(R.id.passwordEdTxt);
    }

    private void setEventHandlers() {
        enterBtn.setOnClickListener(enterBtnClickListener);
//        loginLayout.setOnFocusChangeListener(edTextFocusChangeListener);
//        passwordLayout.setOnFocusChangeListener(edTextFocusChangeListener);
        //loginET.setOnFocusChangeListener(edTextFocusChangeListener);
        //passwordET.setOnFocusChangeListener(edTextFocusChangeListener);
        loginEdTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    //loginLayout.setError(getString(R.string.required_input));
                    loginLayout.setErrorEnabled(false);
                }
            }
        });
        passwordEdTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    //passwordLayout.setError(getString(R.string.required_input));
                    passwordLayout.setErrorEnabled(false);
                }
            }
        });

        loginEdTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && loginEdTxt.getText().toString().isEmpty()) {
                    loginLayout.setError(getString(R.string.required_input));
                }
            }
        });
        passwordEdTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && passwordEdTxt.getText().toString().isEmpty()) {
                    passwordLayout.setError(getString(R.string.required_input));
                }
            }
        });
    }

    private boolean validateEmail(String email) {
        return email.matches("[a-zA-Z0-9._-]+@([a-z]+\\.)+[a-z]+");
    }


    /*private void configAdapter() {
        //mailServicesRecyclerView = (RecyclerView) findViewById(R.id.recyclerView2);
        mailServicesRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        ImageView gmailImg = new ImageView(this);
        gmailImg.setImageResource(R.drawable.gmail);
        mailServicesRecyclerView.setLayoutManager(layoutManager);
        MailServiceAdapter mAdapter = new MailServiceAdapter(this);
        mailServicesRecyclerView.setAdapter(mAdapter);
    }*/

    private View.OnClickListener enterBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            String login = loginET.getText().toString();
//            String password = passwordET.getText().toString();

            String login = loginEdTxt.getText().toString();
            String password = passwordEdTxt.getText().toString();

            loginLayout.setErrorEnabled(false);
            passwordLayout.setErrorEnabled(false);

            if (login.isEmpty()) {
                loginLayout.setError(getString(R.string.required_input));
                passwordEdTxt.setText("");
                return;
            }
            if (!validateEmail(login)) {
                loginLayout.setError(getString(R.string.enter_valid_email));
                return;
            }

            if (password.isEmpty()) {
                passwordLayout.setError(getString(R.string.required_input));
                passwordEdTxt.setText("");
                return;
            }

            new Repository(LoginActivity.this)
                    .checkAuthenticationData(login, password)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isValidAuthenticationData -> {
                        if (isValidAuthenticationData) {
                            accountManager.setActiveUser(login, password, MailServiceEnum.GMAIL);
                            Intent startShowMessagesIntent = new Intent(LoginActivity.this, ShowMailsListActivity.class);
                            startActivity(startShowMessagesIntent);
                        } else {
//                            passwordET.setText("");
//                            passwordET.getBackground().setTint(getColor(R.color.red));
//                            if (loginET.getText().toString().equals("")) {
//                                loginET.getBackground().setTint(getColor(R.color.red));
//                            }

                            passwordLayout.setError(getString(R.string.invalid_password));
                            passwordEdTxt.setText("");
                        }

                    });
        }
    };

    private View.OnFocusChangeListener edTextFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus && ((EditText) view).getText().toString().equals("")) {
                //((EditText)view).setHintTextColor(getResources().getColor(R.color.red));
                //((EditText) view).getBackground().setTint(getColor(R.color.red));
                ((TextInputLayout) view).setError("");

            } else {
                //((EditText)view).setHintTextColor(getResources().getColor(R.color.grey));
                //((EditText) view).getBackground().setTint(getColor(R.color.grey));
                ((TextInputLayout) view).setError("");
                ((TextInputLayout) view).setErrorEnabled(false);
            }
        }
    };
}
