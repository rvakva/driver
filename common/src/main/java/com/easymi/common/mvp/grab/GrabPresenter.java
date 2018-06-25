package com.easymi.common.mvp.grab;

import android.content.Context;
import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.easymi.common.entity.MultipleOrder;
import com.easymi.common.result.MultipleOrderResult;
import com.easymi.component.Config;
import com.easymi.component.network.ErrCode;
import com.easymi.component.network.HaveErrSubscriberListener;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.utils.EmUtil;

import java.util.List;

import rx.Observable;

/**
 * Created by developerLzh on 2017/11/22 0022.
 */

public class GrabPresenter implements GrabContract.Presenter {

    private GrabContract.Model model;
    private GrabContract.View view;

    private Context context;

    public GrabPresenter(Context context, GrabContract.View view) {
        this.context = context;
        this.view = view;
        this.model = new GrabModel();
    }

    @Override
    public void queryOrder(Long orderId) {
        Observable<MultipleOrderResult> observable = model.queryOrder(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, true, false, new HaveErrSubscriberListener<MultipleOrderResult>() {
            @Override
            public void onNext(MultipleOrderResult multipleOrderResult) {
                multipleOrderResult.order.addresses = multipleOrderResult.address;
                view.showBase(multipleOrderResult.order);
            }

            @Override
            public void onError(int code) {
                view.showBase(null);
            }
        })));
    }

    @Override
    public void grabOrder(Long orderId) {
        Observable<MultipleOrderResult> observable = model.grabOrder(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, true, false, new HaveErrSubscriberListener<MultipleOrderResult>() {
            @Override
            public void onNext(MultipleOrderResult multipleOrderResult) {
                MultipleOrder order = multipleOrderResult.order;
                if (order != null) {
                    if (order.orderType.equals(Config.DAIJIA)) {
                        ARouter.getInstance()
                                .build("/daijia/FlowActivity")
                                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .withLong("orderId", order.orderId).navigation();
                    }
                }
                view.finishActivity();
            }

            @Override
            public void onError(int code) {
                view.showBase(null);
                if (code == ErrCode.NOT_MATCH.getCode()
                        || code == ErrCode.GRAB_ORDER_ERROR.getCode()
                        || code == ErrCode.DRIVER_GOTO_PRE_ORDER_CODE.getCode()) {
                    view.removerOrderById(orderId);
                }
            }
        })));
    }

    @Override
    public void takeOrder(Long orderId) {
        Observable<MultipleOrderResult> observable = model.takeOrder(orderId);

        view.getManager().add(observable.subscribe(new MySubscriber<>(context, true, false, new HaveErrSubscriberListener<MultipleOrderResult>() {
            @Override
            public void onNext(MultipleOrderResult multipleOrderResult) {
                MultipleOrder order = multipleOrderResult.order;
                if (order != null) {
                    if (order.orderType.equals(Config.DAIJIA)) {
                        ARouter.getInstance()
                                .build("/daijia/FlowActivity")
                                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .withLong("orderId", order.orderId).navigation();
                    }
                }
                view.finishActivity();
            }

            @Override
            public void onError(int code) {
                view.showBase(null);
                if (code == ErrCode.NOT_MATCH.getCode()
                        || code == ErrCode.GRAB_ORDER_ERROR.getCode()
                        || code == ErrCode.DRIVER_GOTO_PRE_ORDER_CODE.getCode()) {
                    view.removerOrderById(orderId);
                }
            }
        })));
    }

    RouteSearch routeSearch;

    @Override
    public void routePlanByRouteSearch(LatLonPoint endPoint, List<LatLonPoint> pass) {
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

        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, endPoint);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo,
                RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST, pass, null, "");
        routeSearch.calculateDriveRouteAsyn(query);
    }
}
