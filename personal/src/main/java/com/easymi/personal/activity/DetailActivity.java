package com.easymi.personal.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.entity.Employ;
import com.easymi.component.utils.EmUtil;
import com.easymi.personal.R;
import com.easymi.personal.adapter.DetailAdapter;
import com.easymi.personal.entity.Detail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by developerLzh on 2017/11/11 0011.
 */

public class DetailActivity extends RxBaseActivity {

    RecyclerView recyclerView;

    DetailAdapter adapter;

    TextView balanceText;

    @Override
    public int getLayoutId() {
        return R.layout.activity_detail;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        recyclerView = findViewById(R.id.recyclerView);
        balanceText = findViewById(R.id.balance_text);
        adapter = new DetailAdapter(this);

        Employ employ = EmUtil.getEmployInfo();
        balanceText.setText(String.valueOf("¥"+employ.balance));

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        List<Detail> detailList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Detail detail = new Detail();
            detail.purpose = "费用报销";
            detail.money = 200.2;
            detail.time = "2017-11-11 12:30";
            detailList.add(detail);
        }
        adapter.setList(detailList);
    }
}
