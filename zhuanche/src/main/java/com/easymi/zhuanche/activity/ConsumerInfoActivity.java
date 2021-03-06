package com.easymi.zhuanche.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.easymi.component.Config;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.network.ApiManager;
import com.easymi.component.network.HttpResultFunc;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.utils.EmUtil;
import com.easymi.component.widget.CusToolbar;
import com.easymi.zhuanche.ZCApiService;
import com.easymi.zhuanche.R;
import com.easymi.zhuanche.result.ConsumerResult;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName: ConsumerInfoActivity
 * @Author: shine
 * Date: 2018/12/24 下午1:10
 * Description: 客户信息界面
 * History:
 */
public class ConsumerInfoActivity extends RxBaseActivity {

    TextView consumer_name;
    TextView consumer_type;
    TextView order_company;
    TextView consumer_company;
    TextView consumer_balance;
    TextView can_sign;

    private Long orderId;

    @Override
    public void initToolBar() {
        CusToolbar cusToolbar = findViewById(R.id.cus_toolbar);
        cusToolbar.setTitle(R.string.consumer_msg);
        cusToolbar.setLeftBack(view -> onBackPressed());
    }

    @Override
    public int getLayoutId() {
        return R.layout.zc_activity_consumer_info;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        orderId = getIntent().getLongExtra("orderId", -1);
        consumer_name = findViewById(R.id.consumer_name);
        consumer_type = findViewById(R.id.consumer_type);
        order_company = findViewById(R.id.order_company);
        consumer_company = findViewById(R.id.consumer_company);
        consumer_balance = findViewById(R.id.consumer_balance);
        can_sign = findViewById(R.id.can_sign);

        getConsumerInfo();
    }

    /**
     * 获取客户信息
     */
    private void getConsumerInfo() {
        Observable<ConsumerResult> observable = ApiManager.getInstance().createApi(Config.HOST, ZCApiService.class)
                .getConsumer(orderId, EmUtil.getAppKey())
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this, true, false, consumerResult -> {
            if (consumerResult.consumerInfo != null) {
                consumer_name.setText(consumerResult.consumerInfo.consumerName);
                consumer_type.setText(consumerResult.consumerInfo.consumerType == 1 ? getString(R.string.personal_consumer) : getString(R.string.can_sign_consumer));
                order_company.setText(consumerResult.consumerInfo.orderCompany);
                consumer_company.setText(consumerResult.consumerInfo.consumerCompany);
                consumer_balance.setText(consumerResult.consumerInfo.consumerBalance + getString(R.string.yuan));
                can_sign.setText(consumerResult.consumerInfo.canSign ? getString(R.string.allow) : getString(R.string.not_allow));
            }
        })));
    }

    @Override
    public boolean isEnableSwipe() {
        return true;
    }
}
