package com.easymi.zhuanche.flowMvp;

import android.content.Context;
import android.content.Intent;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.easymi.common.entity.BuildPushData;
//import com.easymi.common.push.MQTTService;
import com.easymi.common.push.MqttManager;
import com.easymi.component.Config;
import com.easymi.component.DJOrderStatus;
import com.easymi.component.ZCOrderStatus;
import com.easymi.component.activity.NaviActivity;
import com.easymi.component.app.XApp;
import com.easymi.component.entity.DymOrder;
import com.easymi.component.network.HaveErrSubscriberListener;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.result.EmResult;
import com.easymi.component.utils.EmUtil;
import com.easymi.component.utils.Log;
import com.easymi.component.utils.PhoneUtil;
import com.easymi.component.utils.ToastUtil;
import com.easymi.component.widget.LoadingButton;
import com.easymi.zhuanche.entity.ZCOrder;
import com.easymi.zhuanche.result.ZCOrderResult;
import com.easymin.driver.securitycenter.utils.AudioUtil;
import com.easymin.driver.securitycenter.utils.CenterUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName:
 * @Author: shine
 * Date: 2018/12/24 下午1:10
 * Description:
 * History:
 */
public class FlowPresenter implements FlowContract.Presenter, INaviInfoCallback, AMapNaviListener {

    private Context context;

    private FlowContract.View view;
    private FlowContract.Model model;

    public FlowPresenter(Context context, FlowContract.View view) {
        this.context = context;
        this.view = view;
        model = new FlowModel();
    }

    //接单
    @Override
    public void acceptOrder(Long orderId, Long version, LoadingButton btn) {
        Observable<ZCOrderResult> observable = model.doAccept(orderId, version);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, btn, zcOrderResult -> {
            findOne(orderId);
        })));
    }

    //拒单
    @Override
    public void refuseOrder(Long orderId, String orderType, String remark) {
        Observable<EmResult> observable = model.refuseOrder(orderId, orderType, remark);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, true, true, zcOrderResult -> {
            DymOrder dymOrder = DymOrder.findByIDType(orderId, Config.DAIJIA);
            if (null != dymOrder) {
                dymOrder.delete();
            }
            view.refuseSuc();
        })));
    }

    //前往预约地
    @Override
    public void toStart(Long orderId, Long version, LoadingButton btn) {
        Observable<ZCOrderResult> observable = model.toStart(orderId, version);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, btn, zcOrderResult -> {
            findOne(orderId);
        })));
    }

    //到达预约地
    @Override
    public void arriveStart(Long orderId, Long version) {
        Observable<ZCOrderResult> observable = model.arriveStart(orderId, version);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, false, false, zcOrderResult -> {
            findOne(orderId);
        })));
    }

    @Override
    public void startWait(Long orderId, LoadingButton btn) {
        Observable<ZCOrderResult> observable = model.startWait(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, btn, zcOrderResult -> {
            findOne(orderId);
        })));
    }

    //开始中途等待 达到预约地的
    @Override
    public void startWait(Long orderId) {
        Observable<ZCOrderResult> observable = model.startWait(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, false, false, zcOrderResult -> {
            findOne(orderId);
        })));
    }

    //前往目的地
    @Override
    public void startDrive(Long orderId, Long version) {
        if (!PhoneUtil.checkGps(context)) {
            return;
        }
        Observable<ZCOrderResult> observable = model.startDrive(orderId, version);
        view.getManager().add(observable.subscribe(new MySubscriber<ZCOrderResult>(context,false,false,  zcOrderResult -> {
            updateDymOrder(zcOrderResult.data);
            view.showOrder(zcOrderResult.data);
        })));
    }


    //到达目的地
    @Override
    public void arriveDes(ZCOrder zcOrder, Long version, LoadingButton btn, DymOrder dymOrder) {

        Observable<ZCOrderResult> observable = model.arriveDes(zcOrder, dymOrder, version);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, btn, zcOrderResult -> {
            updateDymOrder(zcOrderResult.data);
            view.showOrder(zcOrderResult.data);
            //todo 一键报警
//            AudioUtil audioUtil = new AudioUtil();
//            audioUtil.onRecord(context, false);
        })));
    }

    @Override
    public void navi(LatLng latLng, String poi, Long orderId) {
        NaviLatLng start = new NaviLatLng(EmUtil.getLastLoc().latitude, EmUtil.getLastLoc().longitude);
        NaviLatLng end = new NaviLatLng(latLng.latitude, latLng.longitude);
        Intent intent = new Intent(context, NaviActivity.class);
        intent.putExtra("startLatlng", start);
        intent.putExtra("endLatlng", end);
        intent.putExtra("orderId", orderId);
        intent.putExtra("orderType", Config.ZHUANCHE);

        intent.putExtra(Config.NAVI_MODE, Config.DRIVE_TYPE);

        stopNavi();//停止当前页面的导航，在到导航页时重新初始化导航
        context.startActivity(intent);
    }

    @Override
    public void findOne(Long orderId) {
        Observable<ZCOrderResult> observable = model.findOne(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, false, false, new HaveErrSubscriberListener<ZCOrderResult>() {
            @Override
            public void onNext(ZCOrderResult zcOrderResult) {
                updateDymOrder(zcOrderResult.data);
                view.showOrder(zcOrderResult.data);
            }

            @Override
            public void onError(int code) {
                view.showOrder(null);
            }
        })));
    }

    @Override
    public void findOne(Long orderId, boolean needShowProgress) {
        Observable<ZCOrderResult> observable = model.findOne(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, needShowProgress, false, new HaveErrSubscriberListener<ZCOrderResult>() {
            @Override
            public void onNext(ZCOrderResult zcOrderResult) {
                updateDymOrder(zcOrderResult.data);
                view.showOrder(zcOrderResult.data);
            }

            @Override
            public void onError(int code) {
                view.showOrder(null);
            }
        })));
    }

    @Override
    public void changeEnd(Long orderId, Double lat, Double lng, String address) {
        Observable<ZCOrderResult> observable = model.changeEnd(orderId, lat, lng, address);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, false, false, zcOrderResult -> {
            findOne(orderId);
        })));
    }

    @Override
    public void cancelOrder(Long orderId, String remark) {
        Observable<EmResult> observable = model.cancelOrder(orderId, remark);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, false, true, zcOrderResult -> {
            DymOrder dymOrder = DymOrder.findByIDType(orderId, Config.DAIJIA);
            if (null != dymOrder) {
                dymOrder.delete();
            }
            view.cancelSuc();
        })));
    }

    @Override
    public void onStopSpeaking() {
        XApp.getInstance().stopVoice();
        XApp.getInstance().clearVoiceList();
    }

    AMapNavi mAMapNavi;

    @Override
    public void routePlanByNavi(Double endLat, Double endLng) {
        if (null == mAMapNavi) {
            mAMapNavi = AMapNavi.getInstance(context);
            mAMapNavi.addAMapNaviListener(this);
        }

        int strateFlag = mAMapNavi.strategyConvert(
                XApp.getMyPreferences().getBoolean(Config.SP_CONGESTION, false),
                XApp.getMyPreferences().getBoolean(Config.SP_AVOID_HIGH_SPEED, false),
                XApp.getMyPreferences().getBoolean(Config.SP_COST, false),
                XApp.getMyPreferences().getBoolean(Config.SP_HIGHT_SPEED, false),
                false);

        NaviLatLng start = new NaviLatLng(EmUtil.getLastLoc().latitude, EmUtil.getLastLoc().longitude);
        NaviLatLng end = new NaviLatLng(endLat, endLng);

        List<NaviLatLng> startLs = new ArrayList<>();
        List<NaviLatLng> endLs = new ArrayList<>();

        startLs.add(start);
        endLs.add(end);
        mAMapNavi.calculateDriveRoute(startLs, endLs, null, strateFlag);
    }

    RouteSearch routeSearch;

    @Override
    public void routePlanByRouteSearch(Double endLat, Double endLng) {
        if (null == routeSearch) {
            routeSearch = new RouteSearch(context);
            routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
                @Override
                public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

                }

                @Override
                public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int code) {
                    if (code == 1000) {
                        view.showPath(driveRouteResult);
                    }
                }

                @Override
                public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

                }

                @Override
                public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

                }
            });
        }
        LatLonPoint start = new LatLonPoint(EmUtil.getLastLoc().latitude, EmUtil.getLastLoc().longitude);
        LatLonPoint end = new LatLonPoint(endLat, endLng);

        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo,
                RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST, null, null, "");
        routeSearch.calculateDriveRouteAsyn(query);
    }


    @Override
    public void updateDymOrder(ZCOrder zcOrder) {
        DymOrder dymOrder = DymOrder.findByIDType(zcOrder.orderId, zcOrder.orderType);
        if (null == dymOrder) {
            if (null != zcOrder.orderFee) {
                dymOrder = zcOrder.orderFee;
                dymOrder.orderId = zcOrder.orderId;
                dymOrder.orderType = zcOrder.orderType;
                dymOrder.passengerId = zcOrder.passengerId;
                dymOrder.orderStatus = zcOrder.orderStatus;

                dymOrder.waitTime = zcOrder.orderFee.waitTime / 60;
                dymOrder.travelTime = zcOrder.orderFee.travelTime / 60;
                dymOrder.distance = new BigDecimal(zcOrder.orderFee.distance / 1000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                dymOrder.lowSpeedTime = zcOrder.orderFee.lowSpeedTime / 60;
                dymOrder.nightTime = zcOrder.orderFee.nightTime / 60;
            } else {
                dymOrder = new DymOrder(zcOrder.orderId, zcOrder.orderType,
                        zcOrder.passengerId, zcOrder.orderStatus);
            }
            dymOrder.orderStatus = zcOrder.orderStatus;
            dymOrder.save();
        } else {
            if (null != zcOrder.orderFee) {
                long id = dymOrder.id;
                dymOrder = zcOrder.orderFee;
                dymOrder.id = id;
                dymOrder.orderId = zcOrder.orderId;
                dymOrder.orderType = zcOrder.orderType;
                dymOrder.passengerId = zcOrder.passengerId;
                dymOrder.orderStatus = zcOrder.orderStatus;

                dymOrder.waitTime = zcOrder.orderFee.waitTime / 60;
                dymOrder.travelTime = zcOrder.orderFee.travelTime / 60;
                dymOrder.distance = new BigDecimal(zcOrder.orderFee.distance / 1000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                dymOrder.lowSpeedTime = zcOrder.orderFee.lowSpeedTime / 60;
                dymOrder.nightTime = zcOrder.orderFee.nightTime / 60;
            }
            dymOrder.orderStatus = zcOrder.orderStatus;
            dymOrder.updateAll();
        }
        MqttManager.getInstance().pushLoc(new BuildPushData(EmUtil.getLastLoc()));
    }

    //选择支付类型后的结算接口
    @Override
    public void payOrder(Long orderId, String payType, Long version) {
        Observable<EmResult> observable = model.payOrder(orderId, payType, version);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, true, false, emResult -> {
            DymOrder dymOrder = DymOrder.findByIDType(orderId, Config.DAIJIA);
            if (null != dymOrder) {
                dymOrder.delete();
            }
            view.paySuc();
        })));
    }

    @Override
    public void stopNavi() {
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        if (null != mAMapNavi) {
            mAMapNavi.stopNavi();
            mAMapNavi.destroy();
        }
    }

    @Override
    public void getConsumerInfo(Long orderId) {
        view.getManager().add(model.consumerInfo(orderId).subscribe(new MySubscriber<>(context, true,
                false, consumerResult -> view.showConsumer(consumerResult.consumerInfo))));
    }


    @Override
    public void onInitNaviFailure() {
        Log.e("FlowerPresenter", "onInitNaviFailure()");
        view.reRout();
    }

    @Override
    public void onInitNaviSuccess() {
        Log.e("FlowerPresenter", "onInitNaviSuccess()");
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        Log.e("FlowerPresenter", "onLocationChange()");
    }

    @Override
    public void onGetNavigationText(int i, String s) {
        Log.e("FlowerPresenter", s);
    }

    @Override
    public void onArriveDestination(boolean b) {
        Log.e("FlowerPresenter", "onArriveDestination()");
        XApp.getInstance().syntheticVoice("即将到达目的地");
    }

    @Override
    public void onStartNavi(int i) {
        Log.e("FlowerPresenter", "onStartNavi()");
    }

    @Override
    public void onTrafficStatusUpdate() {
        Log.e("FlowerPresenter", "onTrafficStatusUpdate()");
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        Log.e("FlowerPresenter", "onCalculateRouteSuccess()");
        AMapNaviPath path;
        HashMap<Integer, AMapNaviPath> paths = mAMapNavi.getNaviPaths();
        if (null != paths && paths.size() != 0) {
            path = paths.get(ints[0]);
        } else {
            path = mAMapNavi.getNaviPath();
        }
        if (path != null) {
            view.showPath(ints, path);
            view.showLeft(path.getAllLength(), path.getAllTime());
            if (XApp.getMyPreferences().getBoolean(Config.SP_DEFAULT_NAVI, true)) {
                mAMapNavi.startNavi(NaviType.GPS);
            }
        }
    }

    @Override
    public void notifyParallelRoad(int i) {
        Log.e("FlowerPresenter", "notifyParallelRoad()");
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        Log.e("FlowerPresenter", "OnUpdateTrafficFacility()");
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        Log.e("FlowerPresenter", "OnUpdateTrafficFacility()");
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
        Log.e("FlowerPresenter", "OnUpdateTrafficFacility()");
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        Log.e("FlowerPresenter", "updateAimlessModeStatistics()");
    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        Log.e("FlowerPresenter", "updateAimlessModeCongestionInfo()");
    }

    @Override
    public void onPlayRing(int i) {
        Log.e("FlowerPresenter", "onPlayRing()");
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Log.e("FlowerPresenter", "onCalculateRouteFailure()");
    }

    /**
     * 重新算路前的回调
     */
    @Override
    public void onReCalculateRouteForYaw() {
        Log.e("FlowerPresenter", "onReCalculateRouteForYaw()");
        view.showReCal();
        XApp.getInstance().syntheticVoice("您已偏航，已为您重新规划路径");
    }

    /**
     * 重新算路前的回调
     */
    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {
        Log.e("FlowerPresenter", "onArrivedWayPoint()");
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
        if (!b){
            ToastUtil.showMessage(context,"请打开手机gps");
        }
    }

    /**
     * 导航信息更新
     *
     * @param naviInfo
     */
    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        Log.e("FlowerPresenter", "onNaviInfoUpdate");
        view.showLeft(naviInfo.getPathRetainDistance(), naviInfo.getPathRetainTime());
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
        Log.e("FlowerPresenter", "onNaviInfoUpdated()");
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
        Log.e("FlowerPresenter", "updateCameraInfo()");
    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {
        Log.e("FlowerPresenter", "onServiceAreaUpdate()");
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        Log.e("FlowerPresenter", "showCross()");
    }

    @Override
    public void hideCross() {
        Log.e("FlowerPresenter", "hideCross()");
    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {
        Log.e("FlowerPresenter", "showModeCross()");
    }

    @Override
    public void hideModeCross() {
        Log.e("FlowerPresenter", "hideModeCross()");
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        Log.e("FlowerPresenter", "showLaneInfo()");
    }

    @Override
    public void hideLaneInfo() {
        Log.e("FlowerPresenter", "hideLaneInfo()");
    }

    @Override
    public void onGetNavigationText(String s) {
    }

    @Override
    public void onEndEmulatorNavi() {
        Log.e("FlowerPresenter", "onEndEmulatorNavi()");
    }

    @Override
    public void onArriveDestination() {
        Log.e("FlowerPresenter", "onArriveDestination()");
    }

    public void onExitPage(int i) {
        Log.e("FlowerPresenter", "onExitPage()");
    }

    public void onReCalculateRoute(int i) {
        Log.e("FlowerPresenter", "onReCalculateRoute()");
    }


}
