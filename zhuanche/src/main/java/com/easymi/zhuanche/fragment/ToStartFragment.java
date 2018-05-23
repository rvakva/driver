package com.easymi.zhuanche.fragment;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easymi.component.base.RxBaseFragment;
import com.easymi.component.utils.Log;
import com.easymi.component.widget.LoadingButton;
import com.easymi.zhuanche.R;
import com.easymi.zhuanche.entity.ZCOrder;
import com.easymi.zhuanche.flowMvp.ActFraCommBridge;
import com.easymi.zhuanche.widget.CallPhoneDialog;

/**
 * Created by developerLzh on 2017/11/13 0013.
 */

public class ToStartFragment extends RxBaseFragment {
    private ZCOrder zcOrder;

    private ActFraCommBridge bridge;

    public void setBridge(ActFraCommBridge bridge) {
        this.bridge = bridge;
    }

    LoadingButton controlCon;
    FrameLayout callPhoneCon;
    TextView startPlaceText;
    TextView endPlaceText;
    LinearLayout changEndCon;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        zcOrder = (ZCOrder) args.getSerializable("zcOrder");
    }

    @Override
    public int getLayoutResId() {
        return R.layout.zc_to_start_fragment;
    }

    @Override
    public void finishCreateView(Bundle state) {
        initView();
    }

    private void initView() {
        startPlaceText = getActivity().findViewById(R.id.start_place);
        endPlaceText = getActivity().findViewById(R.id.end_place);
        controlCon = getActivity().findViewById(R.id.to_start_btn);
        callPhoneCon = getActivity().findViewById(R.id.call_phone_con);
        changEndCon = getActivity().findViewById(R.id.change_end_con);

        startPlaceText.setText(zcOrder.startPlace);
        endPlaceText.setText(zcOrder.endPlace);
        controlCon.setOnClickListener(view -> {
            Log.e("tag", "onClick");
            bridge.doToStart(controlCon);
        });
        callPhoneCon.setOnClickListener(view -> {
            Log.e("tag", "onClick");
            CallPhoneDialog.callDialog(getActivity(),zcOrder);
        });
        changEndCon.setOnClickListener(v -> bridge.changeEnd());
    }
}
