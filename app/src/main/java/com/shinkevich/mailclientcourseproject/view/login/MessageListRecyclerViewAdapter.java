package com.shinkevich.mailclientcourseproject.view.login;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shinkevich.mailclientcourseproject.model.Mail;
import com.shinkevich.mailclientcourseproject.R;

import java.util.ArrayList;
import java.util.List;

public class MessageListRecyclerViewAdapter extends RecyclerView.Adapter<MessageListRecyclerViewAdapter.MessageViewHolder> {
    List<Mail> messagesList = new ArrayList<>();

    public MessageListRecyclerViewAdapter(List<Mail> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Mail currentMessage = messagesList.get(position);
        holder.authorTV.setText(currentMessage.getAuthorEmail());
        holder.topicTV.setText(currentMessage.getTopic());
        holder.textTV.setText(currentMessage.getTopic());
        holder.dateTV.setText(currentMessage.getDate());

        // don't show topicTextView, when there is no topic
        if (currentMessage.getTopic().equals("")) {
            holder.topicTV.setVisibility(View.GONE);
            holder.textTV.setMaxLines(3);
        } else {
            holder.topicTV.setVisibility(View.VISIBLE);
            holder.textTV.setMaxLines(2);
        }

        // Different appearance for read and not-read messages
        if (!currentMessage.isRead()) {
            holder.authorTV.setTypeface(holder.authorTV.getTypeface(), Typeface.BOLD);
            holder.topicTV.setTypeface(holder.topicTV.getTypeface(), Typeface.BOLD);
        } else {
            holder.authorTV.setTypeface(null, Typeface.NORMAL);
            holder.topicTV.setTypeface(null, Typeface.NORMAL);
        }

        /*holder.setStarBtnImage(messagesList.get(position).isInFavourites());
        //favouritesBtn.setTag(message); // сохраняем для каждой письмо, к которому она относится
        holder.favouritesButton.setOnClickListener(favouritesBtnClickListener);
        holder.favouritesButton.setFocusable(false);*/
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView authorTV;
        TextView topicTV;
        TextView textTV;
        TextView dateTV;
        ImageButton favouritesButton;

        Context context;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTV = itemView.findViewById(R.id.authorTextView);
            topicTV = itemView.findViewById(R.id.topicTextView);
            textTV = itemView.findViewById(R.id.textTextView);
            dateTV = itemView.findViewById(R.id.authorTextView);
            favouritesButton = itemView.findViewById(R.id.favouritesBtn);

            this.context = itemView.getContext();
        }

        /*private void setStarBtnImage(boolean isInFavourites) {
            if (isInFavourites) {
                favouritesButton.setImageDrawable(context.getDrawable(R.drawable.selected_star_icon));
            } else {
                (favouritesButton.setImageDrawable(context.getDrawable(R.drawable.star_icon));
            }
        }*/
    }
}
