package com.easymin.custombus.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.easymi.common.entity.ManualCreateBean;
import com.easymi.component.Config;
import com.easymi.component.app.XApp;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.network.ApiManager;
import com.easymi.component.network.HttpResultFunc2;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.network.NoErrSubscriberListener;
import com.easymi.component.utils.EmUtil;
import com.easymi.component.utils.TimeUtil;
import com.easymi.component.utils.ToastUtil;
import com.easymi.component.widget.CusToolbar;
import com.easymi.component.widget.switchButton.SwitchButton;
import com.easymin.custombus.DZBusApiService;
import com.easymin.custombus.R;
import com.easymin.custombus.dialog.ManualCreateDialog;
import com.easymin.custombus.entity.manualCreateBean;
import com.google.gson.Gson;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Route(path = "/custombus/ManualCreateActivity")
public class ManualCreateActivity extends RxBaseActivity {

    private CusToolbar manualCreateCtb;
    private TextView manualCreateTvLineSelect;
    private TextView manualCreateTvDateSelect;
    private TextView manualCreateTvTimeSelect;
    private SwitchButton manualCreateSb;
    private TextView manualCreateTvCreate;
    private ManualCreateDialog dialog;
    private int day;
    private FrameLayout manualCreateFl;
    private int isShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean isEnableSwipe() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_manual_create;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        manualCreateTvLineSelect = findViewById(R.id.manualCreateTvLineSelect);
        manualCreateTvDateSelect = findViewById(R.id.manualCreateTvDateSelect);
        manualCreateTvTimeSelect = findViewById(R.id.manualCreateTvTimeSelect);
        manualCreateTvCreate = findViewById(R.id.manualCreateTvCreate);
        manualCreateSb = findViewById(R.id.manualCreateSb);
        manualCreateFl = findViewById(R.id.manualCreateFl);
        String content = XApp.getMyPreferences().getString(Config.SP_MANUAL_DATA, "");
        if (!TextUtils.isEmpty(content)) {
            ManualCreateBean manualCreateBean = new Gson().fromJson(content, ManualCreateBean.class);
            day = manualCreateBean.day;
            isShow = manualCreateBean.isShow;
            manualCreateFl.setVisibility(isShow == 1 ? View.VISIBLE : View.GONE);
        } else {
            ToastUtil.showMessage(this, "数据发生错误");
            finish();
        }
    }

    @Override
    public void initToolBar() {
        super.initToolBar();
        manualCreateCtb = findViewById(R.id.manualCreateCtb);
        manualCreateCtb.setTitle("手动报班")
                .setLeftIcon(R.drawable.ic_arrow_back, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    public void onManualCreateClick(View v) {
        if (v.getId() == R.id.manualCreateTvLineSelect) {
            getDataList();
        } else if (v.getId() == R.id.manualCreateTvDateSelect) {
            showTimePicker(Type.YEAR_MONTH_DAY);
        } else if (v.getId() == R.id.manualCreateTvTimeSelect) {
            showTimePicker(Type.HOURS_MINS);
        }
    }

    private void getDataList() {
        ApiManager.getInstance().createApi(Config.HOST, DZBusApiService.class)
                .findLineListByDriverId(EmUtil.getEmployId())
                .map(new HttpResultFunc2<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySubscriber<List<manualCreateBean>>
                        (this, true, false, new NoErrSubscriberListener<List<manualCreateBean>>() {
                            @Override
                            public void onNext(List<manualCreateBean> manualCreateBeans) {
                                if (!manualCreateBeans.isEmpty()) {
                                    showDialog(manualCreateBeans);
                                } else {
                                    ToastUtil.showMessage(ManualCreateActivity.this, "数据发生错误,请重试");
                                }
                            }
                        }));
    }

    private void showDialog(List<manualCreateBean> manualCreateBeans) {
        if (dialog == null) {
            dialog = new ManualCreateDialog(this, manualCreateBeans, manualCreateBean -> {
                manualCreateTvLineSelect.setText(manualCreateBean.name);
            });
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog.show();
    }

    public void showTimePicker(Type type) {
        new TimePickerDialog.Builder()
                .setCallBack((timePickerView, millseconds) -> {
                    if (type == Type.YEAR_MONTH_DAY) {
                        manualCreateTvDateSelect.setText(TimeUtil.getTime("MM月dd日", millseconds));
                    } else {
                        manualCreateTvTimeSelect.setText(TimeUtil.getTime("HH:mm", millseconds));
                    }
                })
                .setCancelStringId("取消")
                .setSureStringId("确定")
                .setTitleStringId(type == Type.YEAR_MONTH_DAY ? "发车日期" : "发车时间")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + day * 24 * 60 * 60 * 1000)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(ContextCompat.getColor(this, R.color.colorBlue))
                .setType(type)
                .setWheelItemTextSize(12)
                .build()
                .show(getSupportFragmentManager(), "");
    }
}
