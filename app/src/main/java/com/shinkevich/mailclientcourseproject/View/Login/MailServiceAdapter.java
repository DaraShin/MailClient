package com.shinkevich.mailclientcourseproject.View.Login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shinkevich.mailclientcourseproject.R;

public class MailServiceAdapter extends RecyclerView.Adapter<MailServiceAdapter.ViewHolder> {
    int mailServicesNum = 3;
    int[] imgResorsesId = new int[mailServicesNum];
    Context ctx;

    private int checked_idx = 1;

    public MailServiceAdapter(Context ctx) {
        imgResorsesId[0] = R.drawable.mail_ru;
        imgResorsesId[1] = R.drawable.gmail;
        imgResorsesId[2] = R.drawable.yandex_mail2;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_service_image, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageResource(imgResorsesId[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checked_idx = holder.getAdapterPosition();
                notifyDataSetChanged();
            }
        });
        if (checked_idx == position) {
            holder.itemView.setBackgroundColor(ctx.getColor(R.color.green1));
        } else {
            holder.itemView.setBackgroundColor(ctx.getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return imgResorsesId.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.mailServiceImgView);
        }
    }
}
