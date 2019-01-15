package com.easymin.daijia.driver.zyziyunsjdaijia;

import android.content.Context;

import com.alibaba.sdk.android.push.register.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.easymi.component.Config;
import com.easymi.component.utils.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.easymi.common.daemon.DaemonService;
import com.easymi.common.daemon.PuppetReceiver1;
import com.easymi.common.daemon.PuppetReceiver2;
import com.easymi.common.daemon.PuppetService;
import com.easymi.common.push.AliDetailService;
import com.easymi.component.app.XApp;
import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;

/**
 * Created by developerLzh on 2017/11/3 0003.
 */

public class DriverApp extends XApp {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        DaemonClient daemonClient = new DaemonClient(getDaemonConfigurations());//保活client
        daemonClient.onAttachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initCloudChannel();
        if (!isAppProcess()) {//防止多次调用onCreate()
            return;
        }
    }

    /**
     * 开启阿里云推送服务传
     */
    public void initCloudChannel() {
        PushServiceFactory.init(this);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(this, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("DriverApp", "init cloudchannel success");
                PushServiceFactory.getCloudPushService().turnOnPushChannel(null);//打开推送通道
                PushServiceFactory.getCloudPushService().setPushIntentService(AliDetailService.class);

            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("DriverApp", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        // 注册方法会自动判断是否支持小米系统推送，如不支持会跳过注册。
        MiPushRegister.register(this, Config.MI_APPID, Config.MI_APPKEY);
        HuaWeiRegister.register(this);
    }

    /**
     * 保活进程
     * @return
     */
    protected DaemonConfigurations getDaemonConfigurations() {
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                getPackageName() + ":process1",
                DaemonService.class.getCanonicalName(),
                PuppetReceiver1.class.getCanonicalName());

        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                getPackageName() + ":process2",
                PuppetService.class.getCanonicalName(),
                PuppetReceiver2.class.getCanonicalName());

        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }

    /**
     * 进程监听
     */
    class MyDaemonListener implements DaemonConfigurations.DaemonListener {
        @Override
        public void onPersistentStart(Context context) {
            Log.e("daemon", "--onPersistentStart--");
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
            Log.e("daemon", "--onDaemonAssistantStart--");
        }

        @Override
        public void onWatchDaemonDaed() {
            Log.e("daemon", "--onWatchDaemonDaed--");
        }
    }

}
