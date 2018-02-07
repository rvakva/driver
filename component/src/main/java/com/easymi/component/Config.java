package com.easymi.component;

/**
 * Created by developerLzh on 2017/11/3 0003.
 * <p>
 * 系统地址
 */

public class Config {

    /**
     * 主机地址
     */
    public static final String HOST = "http://192.168.0.72:8082/";
//    public static final String HOST = "https://api.xiaokayun.cn/";
    /**
     * 图片服务器地址
     */
    public static final String IMG_SERVER = "http://pic.xiaoka.me/v1/img?img=";
    /**
     * APP_KEY
     */
    public static final String APP_KEY = "1f462eb305a2417c9564f2dfaf89da9c";
    public static final String WX_APPID = "";

    /**
     * 服务协议地址
     */
    public static final String AGREEMENT_URL = "http://192.168.0.72:8082/";
    /**
     * 司机注册地址
     */
    public static final String REGISTER_URL = "http://192.168.0.72:8082/";

    /**
     * MQTT配置
     */
    public static final String MQTT_HOST = "tcp://123.57.84.216:1883";
    public static final String MQTT_USER_NAME = "admin";
    public static final String MQTT_PSW = "dsajkdghj**@@##$$sagdgha";
    public static final String MQTT_PUSH_TOPIC = "/driver";

    /**
     * SharedPrefence 常量配置
     */
    public static final String SP_DRIVERID = "driverId";
    public static final String SP_ISLOGIN = "isLogin";
    public static final String SP_LAST_LOC = "lastLoc";

    public static final String SP_CONGESTION = "congestion";//躲避拥堵
    public static final String SP_AVOID_HIGH_SPEED = "avoidhightspeed";//不走高速
    public static final String SP_COST = "cost";//避免收费
    public static final String SP_HIGHT_SPEED = "hightspeed";//高速优先

    public static final String SP_USER_LANGUAGE = "user_choice_language";
    public static final int SP_LANGUAGE_AUTO = 0x01;   //语言跟随系统
    public static final int SP_SIMPLIFIED_CHINESE = 0x02; //简体中文
    public static final int SP_TRADITIONAL_CHINESE = 0x03; //繁体中文
    public static final int SP_ENGLISH = 0x04; //英文


    public static final String SP_VOICE_ABLE = "voice_able";//能否语音播报
    public static final String SP_SHAKE_ABLE = "shake_able";//能否震动
    public static final String SP_ALWAYS_OREN = "always_oren";//是否始终横屏计价
    public static final String SP_DEFAULT_NAVI = "default_navi";//默认启动导航


    public static final String SP_LOGIN_ACCOUNT = "login_account";//账号
    public static final String SP_LOGIN_PSW = "login_psw";//密码

    public static final String DAIJIA = "daijia";

    public static final String BROAD_CANCEL_ORDER = "com.easymi.v5driver.BROAD_CANCEL_ORDER";
    public static final String BROAD_EMPLOY_STATUS_CHANGE = "com.easymi.v5driver.EMPLOY_STATUS_CHANGE";
    public static final String BROAD_NOTICE = "com.easymi.v5driver.BROAD_NOTICE";
    public static final String BROAD_ANN = "com.easymi.v5driver.BROAD_ANN";

    public static final int FREE_LOC_TIME = 8000;//闲时定位时间 毫秒
    public static final int BUSY_LOC_TIME = 4000;//忙时定位时间 毫秒


    //--------------------------QQ,QQ空间分享部分------------------------------
    public static final String QQ_APP_ID = "1106099902";

    public static final String WX_APP_ID = "wxd96357cacd5174a1"; //微信app id


}
