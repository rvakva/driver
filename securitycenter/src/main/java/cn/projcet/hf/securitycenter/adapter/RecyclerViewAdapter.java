package cn.projcet.hf.securitycenter.adapter;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;

import cn.projcet.hf.securitycenter.R;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName: RecyclerViewAdapter
 * Author: shine
 * Date: 2018/11/28 上午11:33
 * Description:
 * History:
 */
public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    private Context mContext;
    private ArrayList<String> mDataset;

    public RecyclerViewAdapter(Context context, ArrayList<String> objects) {
        this.mContext = context;
        this.mDataset = objects;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        String item = mDataset.get(position);

        viewHolder.name.setText(item);
        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewHolder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
            }
        });

        viewHolder.tv_delete.setOnClickListener(view -> {
            mItemManger.removeShownLayouts(viewHolder.swipeLayout);
            mDataset.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDataset.size());
            mItemManger.closeAllItems();
//            Toast.makeText(view.getContext(), "Deleted " + viewHolder.textViewData.getText().toString() + "!", Toast.LENGTH_SHORT).show();
        });
        viewHolder.tv_edit.setOnClickListener(v -> {

        });

//        mItemManger.bind(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {

        SwipeLayout swipeLayout;
        TextView tv_edit;
        TextView tv_delete;
        LinearLayout lin_item;
        TextView name;
        TextView tv_phone;
        ImageView iv_check;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            swipeLayout =  itemView.findViewById(R.id.swipe);
            tv_edit =  itemView.findViewById(R.id.tv_edit);
            tv_delete =  itemView.findViewById(R.id.tv_delete);
            lin_item =  itemView.findViewById(R.id.lin_item);
            name = itemView.findViewById(R.id.name);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            iv_check = itemView.findViewById(R.id.iv_check);

            lin_item.setOnClickListener(view -> {

            });

            tv_edit.setOnClickListener(view -> {

            });

            tv_delete.setOnClickListener(view -> {

            });
        }
    }

}