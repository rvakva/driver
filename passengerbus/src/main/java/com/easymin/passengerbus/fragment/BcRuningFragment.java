package com.easymin.passengerbus.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easymi.component.base.RxBaseFragment;
import com.easymi.component.widget.CustomSlideToUnlockView;
import com.easymin.passengerbus.R;
import com.easymin.passengerbus.entity.BusStationResult;
import com.easymin.passengerbus.entity.BusStationsBean;
import com.easymin.passengerbus.flowMvp.ActFraCommBridge;

/**
 * 行程中
 */
public class BcRuningFragment extends RxBaseFragment{

    private TextView tvLineAddress;
    private TextView tvTip;
    private LinearLayout lineLayout;
    private TextView tvWaiteTime;
    private CustomSlideToUnlockView slider;
    private LinearLayout controlCon;
    private ActFraCommBridge bridge;
    private BusStationResult result;

    /**
     * 当前状态
     */
    private String curStr;



    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        result = (BusStationResult) args.getSerializable("busLineResult");

    }

    public void setBridge(ActFraCommBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.flow_status_two_step_layout;
    }

    @Override
    public void finishCreateView(Bundle state) {
        initView();
    }

    private void initView() {
        tvLineAddress =  $(R.id.tv_line_address);
        tvTip =  $(R.id.tv_tip);
        lineLayout =  $(R.id.line_layout);
        tvWaiteTime =  $(R.id.tv_waite_time);
        slider =  $(R.id.slider);
        controlCon =  $(R.id.control_con);

        if (result == null) {
            return;
        }

        slider.setOnClickListener(v -> {
            bridge.slideToNext();
        });



        curStr = slider.setHint2("滑动前往下一站");
        slider.setmCallBack(new CustomSlideToUnlockView.CallBack() {
            @Override
            public void onSlide(int distance) {

            }

            @Override
            public void onUnlocked() {

                resetView();
            }
        });

    }

    Handler handler = new Handler();

    private void resetView() {
//        slider.setVisibility(View.GONE);
        if (curStr.equals("滑动前往下一站")) {
            slider.setHint("滑动到达站点");
            curStr = slider.setHint2("滑动到达站点");
        } else if (curStr.equals("滑动到达站点")) {
            slider.setHint("滑动前往下一站");
            curStr = slider.setHint2("滑动前往下一站");
        }

        handler.postDelayed(() -> getActivity().runOnUiThread(() -> {
            slider.resetView();
            slider.setVisibility(View.VISIBLE);
        }), 1000);
        //防止卡顿
    }
}
