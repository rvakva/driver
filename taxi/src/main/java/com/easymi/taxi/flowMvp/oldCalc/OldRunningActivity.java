package com.easymi.taxi.flowMvp.oldCalc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import com.easymi.component.utils.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.services.route.DriveRouteResult;
import com.easymi.common.entity.Address;
import com.easymi.common.push.FeeChangeObserver;
import com.easymi.common.push.HandlePush;
import com.easymi.component.Config;
import com.easymi.component.ZCOrderStatus;
import com.easymi.component.app.XApp;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.entity.DymOrder;
import com.easymi.component.rxmvp.RxManager;
import com.easymi.component.utils.CsSharedPreferences;
import com.easymi.component.widget.LoadingButton;
import com.easymi.taxi.R;
import com.easymi.taxi.entity.ConsumerInfo;
import com.easymi.taxi.entity.TaxiOrder;
import com.easymi.taxi.flowMvp.ActFraCommBridge;
import com.easymi.taxi.flowMvp.FlowActivity;
import com.easymi.taxi.flowMvp.FlowContract;
import com.easymi.taxi.flowMvp.FlowPresenter;
import com.easymi.taxi.util.PhoneUtil;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName: OldRunningActivity
 * @Author: shine
 * Date: 2018/12/24 下午1:10
 * Description: 未使用
 * History:
 */

public class OldRunningActivity extends RxBaseActivity implements FlowContract.View, FeeChangeObserver {

    private LinearLayout back;
    private TextView running_time;
    private TextView distance;
    private TextView total_fee;
    private LoadingButton meter_wait_btn;
    private LoadingButton meter_settle_btn;

    private boolean forceOre;

    private TaxiOrder taxiOrder;
    private long orderId;

    private FlowPresenter presenter;
    /**
     * activity和fragment的通信接口
     */
    private ActFraCommBridge bridge;

    private double payMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//复写onCreate 因为设置虚拟按键必须在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //隐藏虚拟按键
        PhoneUtil.setHideVirtualKey(getWindow());
    }

    @Override
    public int getLayoutId() {
        return R.layout.taxi_activity_old_running;
    }

    @Override
    protected void onStart() {
        super.onStart();
        HandlePush.getInstance().addObserver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        HandlePush.getInstance().deleteObserver(this);
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        mSwipeBackHelper.setSwipeBackEnable(false);//横屏界面不允许侧滑返回

        orderId = getIntent().getLongExtra("orderId", -1);

        forceOre = XApp.getMyPreferences().getBoolean(Config.SP_ALWAYS_OREN, false);
        if (forceOre) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//动态设置为横屏
        }

        presenter = new FlowPresenter(this, this);

        back = findViewById(R.id.back);
        running_time = findViewById(R.id.running_time);
        distance = findViewById(R.id.distance);
        total_fee = findViewById(R.id.total_fee);
        meter_wait_btn = findViewById(R.id.meter_wait_btn);
        meter_settle_btn = findViewById(R.id.meter_settle_btn);

        initBridge();

        showView();

        presenter.findOne(orderId, false);

        back.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OldRunningActivity.this, FlowActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("fromOld", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void feeChanged(long orderId, String orderType) {
        if (taxiOrder == null) {
            return;
        }
        if (orderId == taxiOrder.id && orderType.equals(Config.TAXI)) {
            showView();
        }
    }

    @Override
    public void initToolbar() {

    }

    @Override
    public void initPop() {

    }

    @Override
    public void showTopView() {

    }

    @Override
    public void showToPlace(String toPlace) {

    }

    @Override
    public void showLeftTime(String leftTime) {

    }

    @Override
    public void initBridge() {
        bridge = new ActFraCommBridge() {
            @Override
            public void doAccept(LoadingButton btn) {

            }

            @Override
            public void doRefuse() {

            }

            @Override
            public void doToStart(LoadingButton btn) {
            }

            @Override
            public void doArriveStart() {

            }

            @Override
            public void doStartWait(LoadingButton btn) {
//                presenter.startWait(taxiOrder.orderId, btn);
            }

            @Override
            public void doStartWait() {
//                presenter.startWait(taxiOrder.orderId);
            }

            @Override
            public void doStartDrive(LoadingButton btn) {

            }

            @Override
            public void changeEnd() {

            }

            @Override
            public void doFinish() {
                finish();
            }

            @Override
            public void doQuanlan() {
                showMapBounds();
            }

            @Override
            public void doRefresh() {

            }

            @Override
            public void doUploadOrder() {

            }

            @Override
            public void showDrive() {

            }

            @Override
            public void showCheating() {

            }

            @Override
            public void showSettleDialog() {

            }
        };
    }

    @Override
    public void showBottomFragment(TaxiOrder taxiOrder) {

    }

    @Override
    public void showOrder(TaxiOrder taxiOrder) {
        if (null == taxiOrder) {
            finish();
            return;
        }
        this.taxiOrder = taxiOrder;
        meter_wait_btn.setOnClickListener(view -> presenter.startWait(orderId, meter_wait_btn));
        meter_settle_btn.setOnClickListener(view -> {
            Intent intent = new Intent(OldRunningActivity.this, FlowActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("fromOld", true);
            intent.putExtra("showSettle", true);
            startActivity(intent);
            finish();
        });
        if (taxiOrder.status == ZCOrderStatus.START_WAIT_ORDER) {
            Intent intent = new Intent(OldRunningActivity.this, OldWaitActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void initMap() {

    }

    @Override
    public void showMapBounds() {

    }

    @Override
    public void cancelSuc() {

    }

    @Override
    public void refuseSuc() {

    }

    @Override
    public void showPath(int[] ints, AMapNaviPath path) {

    }

    @Override
    public void showPath(DriveRouteResult result) {

    }

    @Override
    public void showPayType(double money, ConsumerInfo consumerInfo) {

    }

    @Override
    public void paySuc() {

    }

    @Override
    public void showLeft(int dis, int time) {

    }

    @Override
    public void showReCal() {

    }

    @Override
    public void showToEndFragment() {

    }

    @Override
    public void showConsumer(ConsumerInfo consumerInfo) {
        showPayType(payMoney, consumerInfo);
    }

    @Override
    public void hideTops() {

    }

    @Override
    public void settleSuc() {

    }

    @Override
    public RxManager getManager() {
        return mRxManager;
    }

    private void showView() {
        DymOrder dymOrder = DymOrder.findByIDType(orderId, Config.ZHUANCHE);
        if (dymOrder == null) {
            return;
        }
        running_time.setText(String.valueOf(dymOrder.travelTime));
        distance.setText(String.valueOf(dymOrder.distance));
        total_fee.setText(String.valueOf(dymOrder.totalFee));
        meter_wait_btn.setText(getString(R.string.waited) + dymOrder.waitTime + getString(R.string.minutes));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e("lifecycle", "onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
        if (System.currentTimeMillis() - lastChangeTime > 1000) {
            lastChangeTime = System.currentTimeMillis();
        } else {//有的胎神手机这个方法要回调两次
            return;
        }
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {//横屏

        } else {//竖屏
            onBackPressed();
        }
    }

    /**
     * 导航
     *
     * @param view
     */
    public void toNavi(View view) {
        presenter.navi(new LatLng(getEndAddr().latitude, getEndAddr().longitude), getEndAddr().poi, orderId);
    }

    private Address getEndAddr() {
        Address endAddr = null;
        if (taxiOrder != null && taxiOrder.orderAddressVos != null && taxiOrder.orderAddressVos.size() != 0) {
            for (Address address : taxiOrder.orderAddressVos) {
                if (address.type == 3) {
                    endAddr = address;
                    break;
                }
            }
        }
        return endAddr;
    }

    @Override
    public boolean isEnableSwipe() {
        return false;
    }
}
