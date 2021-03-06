package com.easymi.common.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easymi.common.R;
import com.easymi.common.entity.MultipleOrder;

import java.util.ArrayList;
import java.util.List;

import co.lujun.androidtagview.ColorFactory;
import co.lujun.androidtagview.TagContainerLayout;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName:
 * @Author: shine
 * Date: 2018/12/24 下午5:00
 * Description:
 * History:
 */

public class GrabAdapter extends PagerAdapter {

    private List<MultipleOrder> baseOrderList;
    private Context context;

    /**
     * View重用缓存列表
     */
    private List<View> mViewList = new ArrayList<>();

    public GrabAdapter(Context context) {
        this.context = context;
        baseOrderList = new ArrayList<>();
    }

    public void setDJOrderList(List<MultipleOrder> baseOrderList) {
        this.baseOrderList = baseOrderList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return baseOrderList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view;
        final Holder holder;

        if (mViewList.isEmpty()) {
            //不存在可重用的视图，new 视图
            holder = new Holder();
            view = View.inflate(context, R.layout.grab_item, null);
            holder.startPlace = view.findViewById(R.id.start_place);
            holder.endPlace = view.findViewById(R.id.end_place);
            holder.orderTimeText = view.findViewById(R.id.order_time_text);
            holder.tagContainer = view.findViewById(R.id.tag_container);

            view.setTag(holder);
        } else {
            //存在可重用的视图
            //获取可重用的视图，并从缓存中移除
            view = mViewList.remove(0);
            holder = (Holder) view.getTag();
        }

        //设置view上相关数据
        MultipleOrder multipleOrder = baseOrderList.get(position);
        holder.startPlace.setText(multipleOrder.bookAddress);
        holder.endPlace.setText(multipleOrder.destination);
        holder.orderTimeText.setText(multipleOrder.isBookOrder == 1 ? context.getString(R.string.yuyue) : context.getString(R.string.jishi));
        holder.tagContainer.setTheme(ColorFactory.NONE);
        holder.tagContainer.removeAllTags();
//        holder.tagContainer.addTag("不要抽烟");
//        holder.tagContainer.addTag("带手套开车");

        //添加到ViewPager中，并显示
        container.addView(view);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        //从ViewPager中移除view
        container.removeView(view);
        //移除的view添加到回收缓存列表
        mViewList.add(view);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * 销毁Adapter里所有资源
     */
    public void destroy() {
        mViewList.clear();  //清空缓存队列
    }

    class Holder {
        TextView startPlace;
        TextView endPlace;
        TextView orderTimeText;
        TagContainerLayout tagContainer;
    }
}
