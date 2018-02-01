package com.easymi.daijia.flowMvp.oldCalc;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.services.route.DriveRouteResult;
import com.easymi.common.push.FeeChangeObserver;
import com.easymi.common.push.HandlePush;
import com.easymi.component.Config;
import com.easymi.component.DJOrderStatus;
import com.easymi.component.app.XApp;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.entity.DymOrder;
import com.easymi.component.rxmvp.RxManager;
import com.easymi.component.utils.DensityUtil;
import com.easymi.component.utils.ToastUtil;
import com.easymi.component.widget.LoadingButton;
import com.easymi.daijia.R;
import com.easymi.daijia.entity.DJOrder;
import com.easymi.daijia.flowMvp.ActFraCommBridge;
import com.easymi.daijia.flowMvp.FlowActivity;
import com.easymi.daijia.flowMvp.FlowContract;
import com.easymi.daijia.flowMvp.FlowPresenter;
import com.easymi.daijia.fragment.SettleFragmentDialog;
import com.easymi.daijia.util.PhoneUtil;

/**
 * Created by liuzihao on 2018/1/30.
 */

public class OldRunningActivity extends RxBaseActivity implements FlowContract.View, FeeChangeObserver {

    private LinearLayout back;
    private TextView running_time;
    private TextView distance;
    private TextView total_fee;
    private LoadingButton meter_wait_btn;
    private LoadingButton meter_settle_btn;

    private boolean forceOre;

    private DJOrder djOrder;
    private long orderId;

    private FlowPresenter presenter;

    private ActFraCommBridge bridge;

    SettleFragmentDialog settleDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {//复写onCreate 因为设置虚拟按键必须在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        PhoneUtil.setHideVirtualKey(getWindow());//隐藏虚拟按键
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_old_running;
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

        presenter.findOne(orderId,false);

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
        if (djOrder == null) {
            return;
        }
        if (orderId == djOrder.orderId && orderType.equals(Config.DAIJIA)) {
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
                presenter.startWait(djOrder.orderId, btn);
            }

            @Override
            public void doStartWait() {
                presenter.startWait(djOrder.orderId);
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
            public void doConfirmMoney(LoadingButton btn, DymOrder dymOrder) {
                presenter.arriveDes(btn, dymOrder);
            }

            @Override
            public void doPay() {
                showPayType();
            }

            @Override
            public void showSettleDialog() {

            }
        };
    }

    @Override
    public void showBottomFragment(DJOrder djOrder) {

    }

    @Override
    public void showOrder(DJOrder djOrder) {
        if (null == djOrder) {
            finish();
            return;
        }
        this.djOrder = djOrder;
        meter_wait_btn.setOnClickListener(view -> presenter.startWait(orderId, meter_wait_btn));
        meter_settle_btn.setOnClickListener(view -> {
            settleDialog = new SettleFragmentDialog(OldRunningActivity.this, djOrder, bridge);
            settleDialog.show();
        });
        if (djOrder.orderStatus == DJOrderStatus.START_WAIT_ORDER) {
            Intent intent = new Intent(OldRunningActivity.this, OldWaitActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        } else if (djOrder.orderStatus == DJOrderStatus.ARRIVAL_DESTINATION_ORDER) {
            if (settleDialog != null && settleDialog.isShowing()) {
                settleDialog.setDjOrder(djOrder);
            } else {
                settleDialog = new SettleFragmentDialog(this, djOrder, bridge);
                settleDialog.show();
            }
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
    public void showPayType() {
        RadioGroup group = new RadioGroup(this);
        RadioButton helpPayBtn;
        RadioButton balanceBtn;
        RadioButton qiandanBtn;

        helpPayBtn = new RadioButton(this);
        helpPayBtn.setText(getString(R.string.help_pay));

        balanceBtn = new RadioButton(this);
        balanceBtn.setText(getString(R.string.cus_balance));

        qiandanBtn = new RadioButton(this);
        qiandanBtn.setText(getString(R.string.qiandan));

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DensityUtil.dp2px(this, 20), DensityUtil.dp2px(this, 10),
                DensityUtil.dp2px(this, 20), DensityUtil.dp2px(this, 10));

        group.addView(helpPayBtn, params);
        group.addView(balanceBtn, params);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.pay_title))
                .setView(group)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    if (helpPayBtn.isChecked() || balanceBtn.isChecked() || qiandanBtn.isChecked()) {
                        if (helpPayBtn.isChecked()) {
                            presenter.payOrder(orderId, "helppay");
                        } else if (balanceBtn.isChecked()) {
                            presenter.payOrder(orderId, "balance");
                        }
                        dialog1.dismiss();
                    } else {
                        ToastUtil.showMessage(this, getString(R.string.please_pay_title));
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void paySuc() {
        ToastUtil.showMessage(this, getString(R.string.pay_suc));
        finish();
    }

    @Override
    public void showLeft(int dis, int time) {

    }

    @Override
    public void showReCal() {

    }

    @Override
    public RxManager getManager() {
        return mRxManager;
    }

    private void showView() {
        DymOrder dymOrder = DymOrder.findByIDType(orderId, Config.DAIJIA);
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
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {//横屏

        } else {//竖屏
            onBackPressed();
        }
    }
}
