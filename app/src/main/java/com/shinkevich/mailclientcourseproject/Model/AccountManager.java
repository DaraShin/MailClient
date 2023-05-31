package com.shinkevich.mailclientcourseproject.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountManager {
    public static final String USER_PREFERENCES = "user_details";
    public static final String USER_EMAIL_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String SERVICE_KEY = "mail_service";

    private SharedPreferences sharedPreferences;
    //private Context context;

    public AccountManager(Context context) {
        //this.context = context;
        sharedPreferences = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean needLogin() {
        return !(sharedPreferences.contains(USER_EMAIL_KEY) && sharedPreferences.contains(PASSWORD_KEY));
    }

    public void setActiveUser(String userEmail, String password, MailServiceEnum mailService) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString(USER_EMAIL_KEY, userEmail);
        prefEditor.putString(PASSWORD_KEY, password);
        prefEditor.putString(SERVICE_KEY, mailService.toString());
        prefEditor.commit();
    }

    public User getActiveUser() {
        return new User(sharedPreferences.getString(USER_EMAIL_KEY, ""),
                sharedPreferences.getString(PASSWORD_KEY, ""),
                MailServiceEnum.valueOf(sharedPreferences.getString(SERVICE_KEY, "")));
    }

    public void logout() {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.remove(USER_EMAIL_KEY);
        prefEditor.remove(PASSWORD_KEY);
        prefEditor.commit();
    }


    public class User {
        private String email;
        private String password;
        private MailServiceEnum mailService;

        public User(String email, String password, MailServiceEnum mailService) {
            this.email = email;
            this.password = password;
            this.mailService = mailService;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public MailServiceEnum getMailService() {
            return mailService;
        }
    }
}
