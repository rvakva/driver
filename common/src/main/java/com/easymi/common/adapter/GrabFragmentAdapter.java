package com.easymi.common.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName:
 * @Author: shine
 * Date: 2018/12/24 下午5:00
 * Description:
 * History:
 */

public class GrabFragmentAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> data;

    public GrabFragmentAdapter(FragmentManager fm) {
        super(fm);
        data = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return data.get(position);
    }

    public void setData(List<Fragment> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}
