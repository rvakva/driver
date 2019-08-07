package com.easymi.component.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alipay.sdk.app.PayTask;
import com.easymi.component.entity.PayEvent;
import com.easymi.component.entity.PayResult;
import com.easymi.component.utils.Log;
import com.google.gson.JsonElement;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;

public abstract class RxPayActivity extends RxBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPayResult(PayEvent event) {
        if (event.success) {
            onPaySuc();
        } else {
            onPayFail();
        }
    }

    public abstract void onPaySuc();

    public abstract void onPayFail();

    /**
     * 各种支付回调处理
     */
    protected Handler handler = new Handler(msg -> {
        if (msg.what == 0) {
            PayResult result = new PayResult((String) msg.obj);
            if (result.resultStatus.equals("9000")) {
                onPaySuc();
            } else {
                onPayFail();
            }
        } else {
            onPayFail();
        }
        return true;
    });


    /**
     * 调用支付包充值
     *
     * @param data
     */
    protected void launchZfb(String data) {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(RxPayActivity.this);
                String result = alipay
                        .pay(data, true);

                Message msg = new Message();
                msg.what = 0;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        });
    }

    public void launchWeixin(JsonElement data) {
        JSONObject json;
        try {
            json = new JSONObject(data.toString());
            PayReq req = new PayReq();
            req.appId = json.getString("wx_app_id");
            req.partnerId = json.getString("wx_mch_id");
            req.prepayId = json.getString("wx_pre_id");
            req.nonceStr = json.getString("wx_app_nonce");
            req.timeStamp = json.getString("wx_app_ts");
            req.packageValue = json.getString("wx_app_pkg");
            req.sign = json.getString("wx_app_sign");
            req.extData = "app data"; // optional
            Log.e("wxPay", "正常调起支付");

            IWXAPI api = WXAPIFactory.createWXAPI(RxPayActivity.this, req.appId);
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信

            api.sendReq(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
