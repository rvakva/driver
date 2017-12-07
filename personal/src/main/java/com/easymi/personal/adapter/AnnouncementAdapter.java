package com.easymi.personal.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easymi.component.utils.StringUtils;
import com.easymi.component.utils.TimeUtil;
import com.easymi.personal.R;
import com.easymi.personal.entity.Announcement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by developerLzh on 2017/11/10 0010.
 */

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.NotifityHolder> {

    private Context context;

    private List<Announcement> list;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Announcement announcement);
    }

    public AnnouncementAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public void setList(List<Announcement> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public NotifityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifity_item, parent, false);

        return new NotifityHolder(view);
    }

    @Override
    public void onBindViewHolder(NotifityHolder holder, int position) {
        Announcement notifity = list.get(position);
        holder.notifityContent.setText(notifity.message);
        holder.notifityTime.setText(TimeUtil.getTime("yyyy-MM-dd HH:mm", notifity.time));
        holder.isNew.setVisibility(System.currentTimeMillis() - notifity.time <= (7 * 24 * 60 * 60 * 1000) ? View.VISIBLE : View.GONE);

        if (StringUtils.isNotBlank(notifity.url)) {
            holder.hasMore.setVisibility(View.VISIBLE);
            holder.rootView.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onClick(notifity);
                }
            });
        } else {
            holder.hasMore.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class NotifityHolder extends RecyclerView.ViewHolder {

        TextView notifityContent;
        ImageView isNew;
        TextView notifityTime;
        LinearLayout hasMore;
        View rootView;

        public NotifityHolder(View itemView) {
            super(itemView);
            notifityContent = itemView.findViewById(R.id.notifity_content);
            isNew = itemView.findViewById(R.id.is_new);
            notifityTime = itemView.findViewById(R.id.notifity_time);
            rootView = itemView.findViewById(R.id.rl_root);
            hasMore = itemView.findViewById(R.id.show_has_more);
        }
    }
}