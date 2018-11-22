package com.easymi.cityline.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.easymi.cityline.R;
import com.easymi.cityline.entity.OrderCustomer;
import com.easymi.component.Config;
import com.easymi.component.utils.GlideCircleTransform;

import java.util.List;

/**
 * Created by liuzihao on 2018/11/15.
 */

public class CusListAdapter extends RecyclerView.Adapter<CusListAdapter.ViewHolder> {

    private List<OrderCustomer> orderCustomers;
    private Context context;

    private OrderCustomer lastCustomer;

    public CusListAdapter(Context context) {
        this.context = context;
    }

    public void setOrderCustomers(List<OrderCustomer> orderCustomers) {
        this.orderCustomers = orderCustomers;
        lastCustomer = null;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cus_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderCustomer orderCustomer = orderCustomers.get(position);
        holder.sequenceNum.setText(String.valueOf(orderCustomer.num));
        holder.cusName.setText(orderCustomer.name);
        holder.status.setText(orderCustomer.getOrderStatus());

        if (orderCustomer.status == 0 || orderCustomer.status == 3) {
            holder.status.setBackgroundResource(R.drawable.corners_accent_8dp);
        } else {
            holder.status.setBackgroundResource(R.drawable.corners_gray_8dp);
        }

        if (lastCustomer == null) {
            if (orderCustomer.status == 0 || orderCustomer.status == 3) {
                holder.status.setText("当前");
                holder.status.setBackgroundResource(R.drawable.corners_orange_8dp);
            }
        } else {
            if (lastCustomer.status != 0 && lastCustomer.status != 3) {
                if (orderCustomer.status == 0 || orderCustomer.status == 3) {
                    holder.status.setText("当前");
                    holder.status.setBackgroundResource(R.drawable.corners_orange_8dp);
                }
            }
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new GlideCircleTransform())
                .placeholder(R.mipmap.photo_default)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(context)
                .load(Config.IMG_SERVER + orderCustomer.photo + Config.IMG_PATH)
                .apply(options)
                .into(holder.cusPhoto);

        lastCustomer = orderCustomer;
    }

    @Override
    public int getItemCount() {
        return orderCustomers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView sequenceNum;//序号
        ImageView cusPhoto;
        TextView cusName;
        TextView status;

        public ViewHolder(View itemView) {
            super(itemView);
            sequenceNum = itemView.findViewById(R.id.sequence_num);
            cusPhoto = itemView.findViewById(R.id.cus_photo);
            cusName = itemView.findViewById(R.id.cus_name);
            status = itemView.findViewById(R.id.status_text);
        }
    }
}
