package com.easymi.cityline.flowMvp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.easymi.cityline.R;
import com.easymi.cityline.StaticVal;
import com.easymi.cityline.entity.OrderCustomer;
import com.easymi.cityline.flowMvp.ActFraCommBridge;
import com.easymi.component.Config;
import com.easymi.component.ZXOrderStatus;
import com.easymi.component.app.XApp;
import com.easymi.component.base.RxBaseFragment;
import com.easymi.component.entity.DymOrder;
import com.easymi.component.utils.GlideCircleTransform;
import com.easymi.component.utils.PhoneUtil;
import com.easymi.component.widget.CustomSlideToUnlockView;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liuzihao on 2018/11/16.
 */

public class AcceptSendFragment extends RxBaseFragment {

    ImageView customerPhoto;
    TextView customerName;
    TextView customerPhone;
    ImageButton callPhone;
    TextView toPlace;
    ImageView naviBtn;
    LinearLayout countTimeCon;
    TextView countHint;
    TextView countTime;
    CustomSlideToUnlockView slider;

    LinearLayout sliderCon;

    LinearLayout chaoshiCon;
    Button jumpBtn;
    Button acceptedBtn;

    ImageView refreshImg;

    TextView back;

    long orderId;
    String orderType;

    List<OrderCustomer> orderCustomers;

    DymOrder dymOrder;

    ActFraCommBridge bridge;

    private OrderCustomer current;

    public void setBridge(ActFraCommBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showWhatByStatus();
        }
    }

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        if (args == null) {
            return;
        }
        orderId = args.getLong("orderId", 0);
        orderType = args.getString("orderType", "");
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_main_flow;
    }

    @Override
    public void finishCreateView(Bundle state) {
        customerPhoto = $(R.id.customer_photo);
        customerName = $(R.id.customer_name);
        customerPhone = $(R.id.customer_phone);
        callPhone = $(R.id.call_phone);
        toPlace = $(R.id.to_place);
        naviBtn = $(R.id.navi_view);
        countTimeCon = $(R.id.count_time_con);
        countHint = $(R.id.count_hint);
        countTime = $(R.id.count_time);
        slider = $(R.id.slider);
        refreshImg = $(R.id.ic_refresh);
        jumpBtn = $(R.id.jump_accept);
        acceptedBtn = $(R.id.accept_cus);
        chaoshiCon = $(R.id.chaoshi_con);
        back = $(R.id.back);
        sliderCon = $(R.id.slider_con);

        refreshImg.setOnClickListener(v -> {
            bridge.doRefresh();
            refreshImg.setVisibility(View.GONE);
        });

        showWhatByStatus();
    }

    private Timer timer;
    private TimerTask timerTask;

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void showWhatByStatus() {
        cancelTimer();

        dymOrder = DymOrder.findByIDType(orderId, orderType);


        if (dymOrder.orderStatus == ZXOrderStatus.ACCEPT_ING) {
            orderCustomers = OrderCustomer.findByIDTypeOrderByAcceptSeq(orderId, orderType);
        } else {
            orderCustomers = OrderCustomer.findByIDTypeOrderBySendSeq(orderId, orderType);
        }
        Iterator iterator = orderCustomers.iterator();
        while (iterator.hasNext()) {
            OrderCustomer orderCustomer = (OrderCustomer) iterator.next();
            if (orderCustomer.status != 0
                    && orderCustomer.status != 3) {
                iterator.remove();//移除已接、已送、跳过的
            }
        }
        if (orderCustomers.size() != 0) {
            current = orderCustomers.get(0);
            if (current.status == 0) { //未接
                if (current.subStatus == 0) { //未到达预约地
                    countTimeCon.setVisibility(View.GONE);
                    sliderCon.setVisibility(View.VISIBLE);
                    chaoshiCon.setVisibility(View.GONE);
                    back.setVisibility(View.GONE);
                    slider.setHint("滑动到达乘客位置");
                    slider.setmCallBack(new CustomSlideToUnlockView.CallBack() {
                        @Override
                        public void onSlide(int distance) {

                        }

                        @Override
                        public void onUnlocked() {
                            bridge.arriveStart(current);
                            resetView();
                        }
                    });
                } else {
                    countTimeCon.setVisibility(View.VISIBLE);
                    sliderCon.setVisibility(View.GONE);
                    chaoshiCon.setVisibility(View.GONE);
                    back.setVisibility(View.GONE);
                    slider.setHint("滑动确认接到乘客");
                    slider.setmCallBack(new CustomSlideToUnlockView.CallBack() {
                        @Override
                        public void onSlide(int distance) {

                        }

                        @Override
                        public void onUnlocked() {
                            bridge.acceptCustomer(current);
                            resetView();
                        }
                    });
                    initTimer(current);
                }
                showInMap(new LatLng(current.startLat, current.startLng), StaticVal.MARKER_FLAG_START);
            } else if (current.status == 3) {
                countTimeCon.setVisibility(View.GONE);
                sliderCon.setVisibility(View.VISIBLE);
                chaoshiCon.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
                slider.setHint("滑动到达下车点");
                slider.setmCallBack(new CustomSlideToUnlockView.CallBack() {
                    @Override
                    public void onSlide(int distance) {

                    }

                    @Override
                    public void onUnlocked() {
                        bridge.arriveEnd(current);
                        resetView();
                    }
                });
                showInMap(new LatLng(current.endLat, current.endLng), StaticVal.MARKER_FLAG_END);
            }

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .transform(new GlideCircleTransform())
                    .placeholder(R.mipmap.photo_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(getActivity())
                    .load(Config.IMG_SERVER + current.photo + Config.IMG_PATH)
                    .apply(options)
                    .into(customerPhoto);

            callPhone.setOnClickListener(view -> PhoneUtil.call(getActivity(), current.phone));

            customerName.setText(current.name);
            String weihao;
            if (current.phone != null && current.phone.length() > 4) {
                weihao = current.phone.substring(current.phone.length() - 4, current.phone.length());
            } else {
                weihao = current.phone;
            }
            customerPhone.setText("手机尾号：" + weihao);

            toPlace.setText(current.status < 3 ? current.startAddr : current.endAddr);
        }
        bridge.changeToolbar(StaticVal.TOOLBAR_FLOW);

        naviBtn.setOnClickListener(view -> {
            if (current.status == 0) {
                bridge.navi(new LatLng(current.startLat, current.startLng), orderId);
            } else {
                bridge.navi(new LatLng(current.endLat, current.endLng), orderId);
            }
        });
    }

    private void showInMap(LatLng toLatlng, int flag) {

        bridge.clearMap();
        bridge.addMarker(toLatlng, flag);
        bridge.routePath(toLatlng);

    }

    public void mapStatusChanged() {
        refreshImg.setVisibility(View.VISIBLE);
    }

    private long timeSeq = 0;

    private void initTimer(OrderCustomer orderCustomer) {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
        }
        long appoint = orderCustomer.appointTime;
        timeSeq = (appoint - System.currentTimeMillis()) / 1000;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                timeSeq--;
                setTimeText();
            }
        };
        timer.schedule(timerTask, 0, 1000);
        setTimeText();
    }

    private void setTimeText() {
        getActivity().runOnUiThread(() -> {

            StringBuilder sb = new StringBuilder();
            int minute = (int) (Math.abs(timeSeq) / 60);
            int sec = (int) (Math.abs(timeSeq) % 60);
            if (minute < 10) {
                sb.append("0");
            }
            sb.append(minute).append("分");
            if (sec < 10) {
                sb.append("0");
            }
            sb.append(sec).append("秒");
            if (timeSeq < 0) { //超时
                countHint.setText("等候已超时：");
                countTime.setText(sb.toString());
                countTime.setTextColor(getResources().getColor(R.color.color_red));
                sliderCon.setVisibility(View.GONE);
                chaoshiCon.setVisibility(View.VISIBLE);
                jumpBtn.setOnClickListener(view -> showConfirmFlag(true));
                acceptedBtn.setOnClickListener(view -> showConfirmFlag(false));
            } else { //正常计时
                countHint.setText("等候倒计时：");
                countTime.setText(sb.toString());
                countTime.setTextColor(getResources().getColor(R.color.color_orange));
                sliderCon.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);
                chaoshiCon.setVisibility(View.GONE);
            }
        });
    }

    private void showConfirmFlag(boolean jump) {
        cancelTimer();
        sliderCon.setVisibility(View.VISIBLE);
        countTimeCon.setVisibility(View.GONE);
        chaoshiCon.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(view -> showWhatByStatus());
        if (jump) {
            slider.setHint("滑动跳过乘客");
            slider.setmCallBack(new CustomSlideToUnlockView.CallBack() {
                @Override
                public void onSlide(int distance) {

                }

                @Override
                public void onUnlocked() {
                    bridge.jumpAccept(current);
                    resetView();
                }
            });
        } else {
            slider.setHint("滑动接到乘客");
            slider.setmCallBack(new CustomSlideToUnlockView.CallBack() {
                @Override
                public void onSlide(int distance) {

                }

                @Override
                public void onUnlocked() {
                    bridge.acceptCustomer(current);
                    resetView();
                }
            });
        }
    }

    public OrderCustomer getCurrent() {
        return current;
    }

    private boolean speakedHint = false;

    public void showLeft(int dis) {
        if (!speakedHint && dis < 200) { //小于200米
            if (current.status == 0) {
                XApp.getInstance().syntheticVoice("距离上车点还有" + dis + "米");
                XApp.getInstance().shake();
            } else if (current.status == 3) {
                XApp.getInstance().syntheticVoice("距离下车点还有" + dis + "米");
                XApp.getInstance().shake();
            }
            speakedHint = true;
        }
    }

    public void resetSpeakedHint() {
        speakedHint = false;
    }

    Handler handler = new Handler();

    private void resetView() {
        slider.setVisibility(View.GONE);

        handler.postDelayed(() -> getActivity().runOnUiThread(() -> {
            slider.resetView();
            slider.setVisibility(View.VISIBLE);
        }), 1000);
        //防止卡顿
    }

}