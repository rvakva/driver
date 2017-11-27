package com.easymi.component.loc;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceLocation;
import com.easymi.component.Config;
import com.easymi.component.app.XApp;
import com.easymi.component.entity.EmLoc;
import com.easymi.component.utils.ToastUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by developerLzh on 2017/11/18 0018.
 */

public class LocService extends NotiService implements AMapLocationListener {

    public static final String START_LOC = "com.easymi.eomponent.START_LOC";
    public static final String STOP_LOC = "com.easymi.eomponent.STOP_LOC";

    public static final String LOC_CHANGED = "com.easymi.eomponent.LOC_CHANGED";
    public static final String BROAD_TRACE_SUC = "com.easymi.eomponent.BROAD_TRACE_SUC";

    public static int scanTime = 2000;

    /**
     * 处理息屏关掉wifi的delegate类
     */
    private IWifiAutoCloseDelegate mWifiAutoCloseDelegate = new WifiAutoCloseDelegate();
    /**
     * 记录是否需要对息屏关掉wifi的情况进行处理
     */
    private boolean mIsWifiCloseable = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent && intent.getAction().equals(START_LOC)) {
            applyNotiKeepMech(); //开启利用notification提高进程优先级的机制
            if (mWifiAutoCloseDelegate.isUseful(getApplicationContext())) {
                mIsWifiCloseable = true;
                mWifiAutoCloseDelegate.initOnServiceStarted(getApplicationContext());
            }
            startLoc();
            return START_STICKY;
        } else {
            stopLoc();
        }
        return START_NOT_STICKY;
    }

    AMapLocationClient locClient;

    private void startLoc() {
        if (locClient == null) {
            locClient = new AMapLocationClient(this);
            locClient.setLocationListener(this);
        } else {
            locClient.stopLocation();
        }
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption()
                .setInterval(scanTime)
                .setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
                .setNeedAddress(true)
                .setMockEnable(false)
                .setSensorEnable(true);
        locClient.setLocationOption(mLocationOption);

        locClient.startLocation();
    }

    private void stopLoc() {
        if (null != locClient) {
            locClient.stopLocation();
            locClient.onDestroy();
        }
    }

    LBSTraceClient lbsTraceClient;

    private void startTrace() {

        if (null == lbsTraceClient) {
            lbsTraceClient = LBSTraceClient.getInstance(this);
            lbsTraceClient.startTrace((list, list1, s) -> {
                ToastUtil.showMessage(LocService.this, "纠偏失败：-->" + s);

                List<LatLng> originalLatlngs = new ArrayList<>();
                for (TraceLocation traceLocation : list) {
                    LatLng latLng = new LatLng(traceLocation.getLatitude(), traceLocation.getLongitude());
                    originalLatlngs.add(latLng);
                }

                Intent intent = new Intent();
                intent.setAction(BROAD_TRACE_SUC);
                intent.putExtra("traceLatLngs", new Gson().toJson(list1));
                intent.putExtra("originalLatlngs", new Gson().toJson(originalLatlngs));
                sendBroadcast(intent);
            });
        }

//        List<TraceLocation> traceLocations = new ArrayList<>();
//        for (EmLoc loc : locs) {
//            TraceLocation traceLocation = new TraceLocation();
//            traceLocation.setBearing(loc.bearing);
//            traceLocation.setLatitude(loc.latitude);
//            traceLocation.setLongitude(loc.longitude);
//            traceLocation.setSpeed(loc.speed);
//            traceLocation.setTime(loc.locTime);
//            traceLocations.add(traceLocation);
//        }

//        lbsTraceClient.queryProcessedTrace(lineId, traceLocations, LBSTraceClient.TYPE_AMAP, new TraceListener() {
//            @Override
//            public void onRequestFailed(int lineId, String errInfo) {
//                Intent intent1 = new Intent();
//                intent1.setAction(BROAD_TRACE_FAILED);
//                intent1.putExtra("originalLoc", new Gson().toJson(locs));
//                intent1.putExtra("reason", errInfo);
//                sendBroadcast(intent1);
//
//                locs.clear();
//            }
//
//            @Override
//            public void onTraceProcessing(int lineId, int index, List<LatLng> list) {
//
//            }
//
//            @Override
//            public void onFinished(int lineId,
//                                   List<LatLng> linepoints,
//                                   int distance,
//                                   int waitingtime) {
//
//                Intent intent = new Intent();
//                intent.setAction(BROAD_TRACE_SUC);
//                intent.putExtra("originalLoc", new Gson().toJson(locs));
//                intent.putExtra("traceLatLngs", new Gson().toJson(linepoints));
//                sendBroadcast(intent);
//
//                locs.clear();
//            }
//        });
    }

    private void stopTrace() {
        if (null != lbsTraceClient) {
            lbsTraceClient.stopTrace();
            lbsTraceClient = null;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                EmLoc locationInfo = EmLoc.ALocToLoc(amapLocation);

                XApp.getPreferencesEditor().putString(Config.SP_LAST_LOC, new Gson().toJson(locationInfo)).apply();//保存上次的位置信息 json格式字符创

                if (XApp.getMyPreferences().getBoolean(Config.SP_NEED_TRACE, false)) {
                    startTrace();
                } else {
                    stopTrace();
                }

                Log.e("locPos", locationInfo.toString());
                Intent intent = new Intent();
                intent.setAction(LOC_CHANGED);
                sendBroadcast(intent);//发送位置变化广播

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
            if (!mIsWifiCloseable) {
                return;
            }

            if (amapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                mWifiAutoCloseDelegate.onLocateSuccess(getApplicationContext(), PowerManagerUtil.getInstance().isScreenOn(getApplicationContext()), NetUtil.getInstance().isMobileAva(getApplicationContext()));
            } else {
                mWifiAutoCloseDelegate.onLocateFail(getApplicationContext(), amapLocation.getErrorCode(), PowerManagerUtil.getInstance().isScreenOn(getApplicationContext()), NetUtil.getInstance().isWifiCon(getApplicationContext()));
            }
        }
    }
}