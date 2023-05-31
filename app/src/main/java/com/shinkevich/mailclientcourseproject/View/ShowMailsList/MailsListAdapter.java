package com.shinkevich.mailclientcourseproject.View.ShowMailsList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.shinkevich.mailclientcourseproject.Model.Mail;
import com.shinkevich.mailclientcourseproject.Model.MailType;
import com.shinkevich.mailclientcourseproject.R;
import com.shinkevich.mailclientcourseproject.View.CommonViewServices;
import com.shinkevich.mailclientcourseproject.ViewModel.ShowMailsListViewModel;

import java.util.ArrayList;

public class MailsListAdapter extends BaseAdapter {
    private Context context;
    private ViewModelStoreOwner viewModelStoreOwner;
    private ItemsNumberChangedListener itemsNumberChangedListener;

    private LayoutInflater inflater; // строит view по layout-файлу

    private ArrayList<Mail> messagesList = new ArrayList<>();

    private boolean deleteNotFavouriteRules = false;
    private boolean showCategory = false;

    public MailsListAdapter(Context context, ViewModelStoreOwner viewModelStoreOwner,
                            ItemsNumberChangedListener itemsNumberChangedListener) {
        this.context = context;
        this.viewModelStoreOwner = viewModelStoreOwner;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemsNumberChangedListener = itemsNumberChangedListener;
    }

    public MailsListAdapter(Context context, ViewModelStoreOwner viewModelStoreOwner,
                            ItemsNumberChangedListener itemsNumberChangedListener, boolean showCategory) {
        this.context = context;
        this.viewModelStoreOwner = viewModelStoreOwner;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemsNumberChangedListener = itemsNumberChangedListener;
        this.showCategory = showCategory;
    }


    public void setMessagesList(ArrayList<Mail> messagesList) {
        this.messagesList = messagesList;
        CommonViewServices.sortMessagesByDate(this.messagesList);
        notifyDataSetChanged();
    }

    public void addMail(Mail mail) {
        messagesList.add(mail);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public Object getItem(int idx) {
        return messagesList.get(idx);
    }

    @Override
    //возвращает id элемента по позиции
    public long getItemId(int idx) {
        return idx;
    }


    @Override
    public View getView(int idx, View view, ViewGroup parentViewGroup) {
        View builtView = view;
        if (view == null) {
            builtView = inflater.inflate(R.layout.message_item, parentViewGroup, false);
        }

        Mail message = (Mail) getItem(idx);

        TextView authorTV = ((TextView) builtView.findViewById(R.id.authorTextView));
        TextView topicTV = ((TextView) builtView.findViewById(R.id.topicTextView));
        TextView textTV = ((TextView) builtView.findViewById(R.id.textTextView));

        if (message.getMailType() == MailType.SENT) {
            if (message.getRecipients() != null && !message.getRecipients().isEmpty()) {
                authorTV.setText("Кому: " + message.getRecipients().get(0));
            } else {
                authorTV.setText("Кому: ");
            }
        } else {
            authorTV.setText(message.getAuthorEmail());
        }

        topicTV.setText(message.getTopic());
        if (message.getTopic() == null) {
            topicTV.setVisibility(View.GONE);
            //maxTextLength = 90;
            textTV.setMaxLines(3);
        } else {
            topicTV.setVisibility(View.VISIBLE);
        }

        textTV.setText(message.getText());

        if (showCategory) {
            TextView categoryTV = (TextView) builtView.findViewById(R.id.categoryTV);
            switch (message.getMailType()) {
                case INCOMING:
                    categoryTV.setText(context.getString(R.string.incoming));
                    break;
                case SENT:
                    categoryTV.setText(context.getString(R.string.sent));
                    break;
                case DRAFT:
                    categoryTV.setText(context.getString(R.string.drafts));
                    break;
                case FAVOURITE:
                    break;
                case SPAM:
                    categoryTV.setText(context.getString(R.string.spam));
                    break;
                case DEFERRED:
                    categoryTV.setText(context.getString(R.string.deffered));
                    break;
            }
            categoryTV.setVisibility(View.VISIBLE);
        }

        ((TextView) builtView.findViewById(R.id.dateTextView)).setText(CommonViewServices.getDateToShow(message.getDate()));

        if (!message.isRead()) {
            authorTV.setTypeface(authorTV.getTypeface(), Typeface.BOLD);
            topicTV.setTypeface(topicTV.getTypeface(), Typeface.BOLD);
        } else {
            authorTV.setTypeface(null, Typeface.NORMAL);
            topicTV.setTypeface(null, Typeface.NORMAL);
        }

        ImageButton favouritesBtn = (ImageButton) builtView.findViewById(R.id.favouritesBtn);
        setStarBtnImage(favouritesBtn, message.isInFavourites());
        favouritesBtn.setTag(message); // сохраняем для каждой письмо, к которому она относится
        favouritesBtn.setOnClickListener(favouritesBtnClickListener);
        favouritesBtn.setFocusable(false);

        builtView.setTag(message);

        return builtView;
    }


    public void updateWhenReadMessage(int idx) {
        messagesList.get(idx).setIsRead(true);
        notifyDataSetChanged();
    }

    private View.OnClickListener favouritesBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View button) {
            Mail mail = (Mail) button.getTag();
            mail.setIsInFavourites(!mail.isInFavourites());
            new ViewModelProvider(viewModelStoreOwner).get(ShowMailsListViewModel.class).favouriteClick(mail);
            if (!mail.isInFavourites() && deleteNotFavouriteRules) {
                messagesList.remove(mail);
                notifyDataSetChanged();
                itemsNumberChangedListener.itemsNumberChanged(MailsListAdapter.this, getCount());
            }
            setStarBtnImage(button, mail.isInFavourites());
        }
    };

    private void setStarBtnImage(View button, boolean isInFavourites) {
        if (isInFavourites) {
            ((ImageButton) button).setImageDrawable(context.getDrawable(R.drawable.selected_star_icon));
        } else {
            ((ImageButton) button).setImageDrawable(context.getDrawable(R.drawable.star_icon));
        }
    }

    public void setDeleteNotFavouriteRules(boolean deleteNotFavouriteRules) {
        this.deleteNotFavouriteRules = deleteNotFavouriteRules;
    }

}
