package com.shinkevich.mailclientcourseproject.View.ShowMailsList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.shinkevich.mailclientcourseproject.Model.AccountManager;
import com.shinkevich.mailclientcourseproject.Model.MailType;
import com.shinkevich.mailclientcourseproject.View.Login.LoginActivity;
import com.shinkevich.mailclientcourseproject.View.ReadMessageActivity;
import com.shinkevich.mailclientcourseproject.View.WriteMessageActivity;
import com.shinkevich.mailclientcourseproject.Model.Mail;

import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.ViewModel.ShowMailsListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class ShowMailsListActivity extends AppCompatActivity implements ItemsNumberChangedListener {
    private TextView categoryTitleTV;
    private TextView dataLoadingTV;
    private ListView mailsListView;
    private FloatingActionButton newMessageBtn;
    private FloatingActionButton menuBtn;
    private FloatingActionButton refreshMailsBtn;
    private TextView userEmailTV;

    private DrawerLayout menuLayout;
    private NavigationView navView;

    private ShowMailsListViewModel viewModel;

    private MailsListAdapter incomingMailsAdapter = null;
    private MailsListAdapter sentMailsAdapter = null;
    private MailsListAdapter draftsAdapter = null;
    private MailsListAdapter favouritesAdapter = null;
    private MailsListAdapter spamAdapter = null;
    private MailsListAdapter deferredAdapter = null;

    //private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_messages_with_menu_activity);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        viewModel = new ViewModelProvider(this).get(ShowMailsListViewModel.class);

        getViews();
        setEventHandlers();
        initAdapters();
        subscribeLoadingLiveData();
        loadDataOnStart();

        //sharedPreferences = getSharedPreferences("user_details", MODE_PRIVATE);

        configNavigationDrawer();
    }

    private void getViews() {
        mailsListView = (ListView) findViewById(R.id.mailsListView);
        newMessageBtn = (FloatingActionButton) findViewById(R.id.newMailBtn);
        menuLayout = (DrawerLayout) findViewById(R.id.menuLayout);
        menuBtn = (FloatingActionButton) findViewById(R.id.menuBtn);
        navView = (NavigationView) findViewById(R.id.side_menu);
        categoryTitleTV = (TextView) findViewById(R.id.categoryTitleTextView);
        dataLoadingTV = (TextView) findViewById(R.id.dataLoadingTV);
        refreshMailsBtn = (FloatingActionButton) findViewById(R.id.refreshMailsBtn);
    }

    private void setEventHandlers() {
        newMessageBtn.setOnClickListener(newMessageBtnClickListener);
        menuBtn.setOnClickListener(menuBtnClickListener);
        mailsListView.setOnItemClickListener(messagesListClickListener);
        refreshMailsBtn.setOnClickListener(refreshMailsBtnClickListener);
    }

    private void initAdapters() {
        incomingMailsAdapter = new MailsListAdapter(getApplicationContext(), this, this);
        sentMailsAdapter = new MailsListAdapter(getApplicationContext(), this, this);
        draftsAdapter = new MailsListAdapter(getApplicationContext(), this, this);
        favouritesAdapter = new MailsListAdapter(getApplicationContext(), this, this, true);
        favouritesAdapter.setDeleteNotFavouriteRules(true);
        spamAdapter = new MailsListAdapter(getApplicationContext(), this, this);
        deferredAdapter = new MailsListAdapter(getApplicationContext(), this, this);
    }

    private void setLoadingObserver(Supplier<MutableLiveData<Integer>> liveDataProvider, int menuItemId) {
        liveDataProvider.get().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer loadingCounter) {
                if (navView.getCheckedItem().getItemId() != menuItemId) {
                    return;
                }
                if (loadingCounter > 0) {
                    dataLoadingTV.setVisibility(View.VISIBLE);
                    //mailsListView.setVisibility(View.GONE);
                } else if (loadingCounter == 0) {
                    dataLoadingTV.setVisibility(View.GONE);
                    //mailsListView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void subscribeLoadingLiveData() {
        setLoadingObserver(viewModel::getIncomingMailsLoading, R.id.incoming_item);
        setLoadingObserver(viewModel::getSentMailsLoading, R.id.sent_item);
        setLoadingObserver(viewModel::getDeferredMailsLoading, R.id.deffered_send_item);
        setLoadingObserver(viewModel::getDraftsMailsLoading, R.id.drafts_item);
        setLoadingObserver(viewModel::getFavouritesMailsLoading, R.id.favourites_item);
        setLoadingObserver(viewModel::getSpamMailsLoading, R.id.spam_item);
    }

    private void loadDataOnStart() {
        observeMailsByCategory(viewModel.getMails(MailType.SENT), sentMailsAdapter, getString(R.string.sent), viewModel::getSentMailsLoading);
        observeMailsByCategory(viewModel.getMails(MailType.DRAFT), draftsAdapter, getString(R.string.drafts), viewModel::getDraftsMailsLoading);
        observeMailsByCategory(viewModel.getMails(MailType.SPAM), spamAdapter, getString(R.string.spam), viewModel::getSpamMailsLoading);

    }

    private void configNavigationDrawer() {
        navView.setNavigationItemSelectedListener(navigationItemSelectedListener);
        navView.setCheckedItem(R.id.incoming_item);
        userEmailTV = navView.getHeaderView(0).findViewById(R.id.userEmailTV);
        userEmailTV.setText(new AccountManager(this).getActiveUser().getEmail());
        //navigationItemSelectedListener.onNavigationItemSelected(navView.getCheckedItem());
    }

    private View.OnClickListener newMessageBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent writeMailActivityIntent = new Intent(ShowMailsListActivity.this, WriteMessageActivity.class);
            startActivity(writeMailActivityIntent);
        }
    };

    private View.OnClickListener menuBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            menuLayout.openDrawer(GravityCompat.START);
        }
    };

    private View.OnClickListener refreshMailsBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (navView.getCheckedItem().getItemId()) {
                case R.id.incoming_item:
                    observeMailsByCategory(viewModel.getMailsFromServer(MailType.INCOMING), incomingMailsAdapter, getString(R.string.incoming), viewModel::getIncomingMailsLoading);
                    break;
                case R.id.sent_item:
                    observeMailsByCategory(viewModel.getMailsFromServer(MailType.SENT), sentMailsAdapter, getString(R.string.sent), viewModel::getSentMailsLoading);
                    break;
                case R.id.deffered_send_item:
                    observeMailsByCategory(viewModel.getDeferred(), deferredAdapter, getString(R.string.deffered), viewModel::getDeferredMailsLoading);
                    break;
                case R.id.drafts_item:
                    observeMailsByCategory(viewModel.getMailsFromServer(MailType.DRAFT), draftsAdapter, getString(R.string.drafts), viewModel::getDraftsMailsLoading);
                    break;
                case R.id.favourites_item:
                    observeMailsByCategory(viewModel.getFavourites(), favouritesAdapter, getString(R.string.favourites), viewModel::getFavouritesMailsLoading);
                    break;
                case R.id.spam_item:
                    observeMailsByCategory(viewModel.getMailsFromServer(MailType.SPAM), spamAdapter, getString(R.string.spam), viewModel::getSpamMailsLoading);
                    break;
            }
            //getMailsByMenuId(navView.getCheckedItem().getItemId());
        }
    };

    private AdapterView.OnItemClickListener messagesListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View clickedView, int position, long id) {
            Mail mailToRead = (Mail) clickedView.getTag();
            ((MailsListAdapter) adapterView.getAdapter()).updateWhenReadMessage(position);
            viewModel.setMessageSeen(mailToRead);
            Intent readMailActivityIntent = new Intent(ShowMailsListActivity.this, ReadMessageActivity.class);
            readMailActivityIntent.putExtra(ReadMessageActivity.mailExtraKey, mailToRead);
            startActivity(readMailActivityIntent);
        }
    };

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.quit_item) {
                viewModel.clearDataOnLogout();
                Intent logoutIntent = new Intent(ShowMailsListActivity.this, LoginActivity.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logoutIntent);
            } else {
                getMailsByMenuId(itemId);
            }
            menuLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    private void getMailsByMenuId(int menuItemId) {
        switch (menuItemId) {
            case R.id.incoming_item:
                //onMenuItemChosen(viewModel::getIncomingMails, incomingMailsAdapter, getString(R.string.incoming));
                if (viewModel.getIncomingMailsLoading().getValue() > 0) {
                    dataLoadingTV.setVisibility(View.VISIBLE);
                    //mailsListView.setVisibility(View.GONE);
                } else {
                    dataLoadingTV.setVisibility(View.GONE);
                    //mailsListView.setVisibility(View.VISIBLE);
                }
                onMenuItemChosen(MailType.INCOMING, incomingMailsAdapter, getString(R.string.incoming), viewModel::getIncomingMailsLoading);
                //MailsListAdapter adapter = viewModel.getIncomingAdapter(ShowMessagesListActivity.this);
                //categoryTitleTV.setText(getString(R.string.incoming) + " (" + adapter.getCount() + ")");
                //messagesListView.setAdapter(adapter);
                break;
            case R.id.sent_item:
                if (viewModel.getSentMailsLoading().getValue() > 0) {
                    dataLoadingTV.setVisibility(View.VISIBLE);
                    //mailsListView.setVisibility(View.GONE);
                } else {
                    dataLoadingTV.setVisibility(View.GONE);
                    //mailsListView.setVisibility(View.VISIBLE);
                }
                //onMenuItemChosen(viewModel::getSentMails, sentMailsAdapter, getString(R.string.sent));
                onMenuItemChosen(MailType.SENT, sentMailsAdapter, getString(R.string.sent), viewModel::getSentMailsLoading);
                break;
            case R.id.deffered_send_item:
                dataLoadingTV.setVisibility(View.GONE);
                //mailsListView.setVisibility(View.VISIBLE);
                mailsListView.setAdapter(deferredAdapter);
                observeMailsByCategory(viewModel.getDeferred(), deferredAdapter, getString(R.string.deffered), viewModel::getDeferredMailsLoading);
                categoryTitleTV.setText(getString(R.string.deffered) + " (" + deferredAdapter.getCount() + ")");
                break;
            case R.id.drafts_item:
                //onMenuItemChosen(viewModel::getDrafts, draftsAdapter, getString(R.string.drafts));
                if (viewModel.getDraftsMailsLoading().getValue() > 0) {
                    dataLoadingTV.setVisibility(View.VISIBLE);
                    //mailsListView.setVisibility(View.GONE);
                } else {
                    dataLoadingTV.setVisibility(View.GONE);
                    //mailsListView.setVisibility(View.VISIBLE);
                }
                onMenuItemChosen(MailType.DRAFT, draftsAdapter, getString(R.string.drafts), viewModel::getDraftsMailsLoading);
                break;
            case R.id.favourites_item:
                dataLoadingTV.setVisibility(View.GONE);
                //mailsListView.setVisibility(View.VISIBLE);
                mailsListView.setAdapter(favouritesAdapter);
                observeMailsByCategory(viewModel.getFavourites(), favouritesAdapter, getString(R.string.favourites), viewModel::getFavouritesMailsLoading);
                categoryTitleTV.setText(getString(R.string.favourites) + " (" + favouritesAdapter.getCount() + ")");
                break;
            case R.id.spam_item:
                if (viewModel.getSpamMailsLoading().getValue() > 0) {
                    dataLoadingTV.setVisibility(View.VISIBLE);
                    //mailsListView.setVisibility(View.GONE);
                } else {
                    dataLoadingTV.setVisibility(View.GONE);
                    //mailsListView.setVisibility(View.VISIBLE);
                }
                //onMenuItemChosen(viewModel::getSpam, spamAdapter, getString(R.string.spam));
                onMenuItemChosen(MailType.SPAM, spamAdapter, getString(R.string.spam), viewModel::getSpamMailsLoading);
                break;
        }
    }

    private String getCurrentCategoryTitle() {
        switch (navView.getCheckedItem().getItemId()) {
            case R.id.incoming_item:
                return getString(R.string.incoming);
            case R.id.sent_item:
                return getString(R.string.sent);
            case R.id.deffered_send_item:
                return getString(R.string.deffered);
            case R.id.drafts_item:
                return getString(R.string.drafts);
            case R.id.favourites_item:
                return getString(R.string.favourites);
            case R.id.spam_item:
                return getString(R.string.spam);
            default:
                return "";
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getMailsByMenuId(navView.getCheckedItem().getItemId());
    }

    private void onMenuItemChosen(MailType mailType, MailsListAdapter adapter, String categoryTitle, Supplier<MutableLiveData<Integer>> dataLoadingProvider) {
        mailsListView.setAdapter(adapter);
        observeMailsByCategory(viewModel.getMails(mailType), adapter, categoryTitle, dataLoadingProvider);
        categoryTitleTV.setText(categoryTitle + " (" + adapter.getCount() + ")");
    }

    private void observeMailsByCategory(
            LiveData<List<Mail>> liveData, MailsListAdapter adapter, String categoryTitle, Supplier<MutableLiveData<Integer>> dataLoadingProvider) {
        liveData.observe(this, new Observer<List<Mail>>() {
            @Override
            public void onChanged(List<Mail> mailsList) {
                adapter.setMessagesList((ArrayList<Mail>) mailsList);
                if (mailsListView.getAdapter().equals(adapter)) {
                    //if(dataLoadingProvider.get().getValue()==0){
                    categoryTitleTV.setText(categoryTitle + " (" + adapter.getCount() + ")");
                    //}else{
                    //    categoryTitleTV.setText(categoryTitle );
                    //}
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (navView.getCheckedItem().getItemId() == R.id.incoming_item) {
            //super.onBackPressed();
            finish();
        } else {
            navView.setCheckedItem(R.id.incoming_item);
            navigationItemSelectedListener.onNavigationItemSelected(navView.getCheckedItem());
        }

    }

    @Override
    public void itemsNumberChanged(MailsListAdapter adapter, int itemsNum) {
        if (mailsListView.getAdapter() == adapter) {
            categoryTitleTV.setText(getCurrentCategoryTitle() + " (" + itemsNum + ")");
        }
    }
}