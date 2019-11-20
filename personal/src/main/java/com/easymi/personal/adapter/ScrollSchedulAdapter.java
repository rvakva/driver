package com.easymi.personal.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easymi.personal.R;

import java.util.ArrayList;

/**
 * @Copyright (C), 2012-2019, Sichuan Xiaoka Technology Co., Ltd.
 * @FileName: ScrollSchedulAdapter
 * @Author: hufeng
 * @Date: 2019/11/21 上午12:31
 * @Description:
 * @History:
 */
public class ScrollSchedulAdapter extends RecyclerView.Adapter<ScrollSchedulAdapter.Holder> {

    public ArrayList<String> scheduls;
    public Context mContext;

    public ScrollSchedulAdapter(Context context) {
        this.mContext = context;
        scheduls = new ArrayList<>();
    }

    /**
     * 设置数据
     * @param data
     */
    public void setScheduls(ArrayList<String> data){
        this.scheduls = data;
        notifyDataSetChanged();
    }

    @Override
    public ScrollSchedulAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scroll_schedul, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScrollSchedulAdapter.Holder holder, int position) {
        if (position == 1 || position == 2){
            holder.tv_schedul_name.setTextColor(mContext.getResources().getColor(R.color.color_222222));
            holder.tv_hint.setVisibility(View.GONE);

            holder.root.setOnClickListener(v -> {
                if (listener != null){
                    listener.onClick();
                }
            });
        }else {
            holder.tv_schedul_name.setTextColor(mContext.getResources().getColor(R.color.color_cccccc));
            holder.tv_hint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return scheduls.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        TextView tv_schedul_name;
        TextView tv_hint;
        ImageView iv_select;
        View root;

        public Holder(View itemView) {
            super(itemView);
            root = itemView;
            tv_schedul_name = itemView.findViewById(R.id.tv_schedul_name);
            tv_hint = itemView.findViewById(R.id.tv_hint);
            iv_select = itemView.findViewById(R.id.iv_select);
        }
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        /**
         * 点击监听
         */
        void onClick();
    }
}
