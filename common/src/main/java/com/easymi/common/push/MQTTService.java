package com.easymi.common.push;

/**
 * Created by developerLzh on 2017/12/13 0013.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.easymi.common.entity.PushMessage;
import com.easymi.component.Config;
import com.easymi.component.entity.EmLoc;
import com.easymi.component.entity.GpsPush;
import com.easymi.component.entity.PushBean;
import com.easymi.component.loc.LocObserver;
import com.easymi.component.loc.LocReceiver;
import com.easymi.component.utils.EmUtil;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT长连接服务
 *
 * @author 一口仨馍 联系方式 : yikousamo@gmail.com
 * @version 创建时间：2016/9/16 22:06
 */
public class MQTTService extends Service implements LocObserver {

    public static final String TAG = MQTTService.class.getSimpleName();

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    //    private String host = "tcp://10.0.2.2:61613";
    private String host = Config.MQTT_HOST;
    private String userName = Config.MQTT_USER_NAME;
    private String passWord = Config.MQTT_PSW;
    private static String pushTopic = Config.MQTT_PUSH_TOPIC;
    private String pullTopic;
    private String clientId = "driver-" + EmUtil.getEmployId();//身份唯一码

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initConn();
        return START_STICKY;
    }

    private void initConn() {
        if (client != null && client.isConnected()) {
            return;
        }
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(false);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        pullTopic = "/driver" + "/" + Config.APP_KEY + "/" + EmUtil.getEmployId();
        Integer qos = 0;
        Boolean retained = false;
        if ((!message.equals("")) || (!pullTopic.equals(""))) {
            try {
                conOpt.setWill(pullTopic, message.getBytes(), qos, retained);
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if (doConnect) {
            doClientConnection();
        }
    }

    /**
     * 推消息
     *
     * @param msg
     */
    public static void publish(String msg) {
        Integer qos = 1;//与后端约定为1
        Boolean retained = true;
        try {
            if (null != client) {
                client.publish(pushTopic, msg.getBytes(), qos, retained);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNomarl()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 订阅myTopic话题
                LocReceiver.getInstance().addObserver(MQTTService.this);
                client.subscribe(pullTopic, 1);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            // 连接失败，重连
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) {

            String str1 = new String(message.getPayload());

            Log.i(TAG, "MqttReceivePull:" + str1);

            new HandlePush(MQTTService.this, str1);//处理推送消息
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
            LocReceiver.getInstance().deleteObserver(MQTTService.this);
        }
    };

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (connectivityManager != null) {
            info = connectivityManager.getActiveNetworkInfo();
        }
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private long lastUploadTime = 0;

    @Override
    public void receiveLoc(EmLoc loc) {
        if (lastUploadTime != 0) {
            if (System.currentTimeMillis() - lastUploadTime < 10 * 1000) {
                return;
            }
        }
        GpsPush gpsPush = new GpsPush();
        if (gpsPush.employ2This()) {
            gpsPush.appKey = Config.APP_KEY;
            gpsPush.lat = loc.latitude;
            gpsPush.lng = loc.longitude;
            PushBean<GpsPush> pushBean = new PushBean<>("gps", gpsPush);
            publish(new Gson().toJson(pushBean));
            Log.e("pushBean", new Gson().toJson(pushBean));
        }
        lastUploadTime = System.currentTimeMillis();
    }
}
