package com.easymi.personal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easymi.component.Config;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.network.ApiManager;
import com.easymi.component.network.HaveErrSubscriberListener;
import com.easymi.component.network.HttpResultFunc;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.network.NoErrSubscriberListener;
import com.easymi.component.result.EmResult;
import com.easymi.component.utils.CodeUtil;
import com.easymi.component.utils.EmUtil;
import com.easymi.component.utils.PhoneUtil;
import com.easymi.component.utils.StringUtils;
import com.easymi.component.utils.ToastUtil;
import com.easymi.component.widget.VerifyCodeView;
import com.easymi.personal.McService;
import com.easymi.personal.R;
import com.easymi.personal.result.PicCodeResult;

import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName: ResetPswActivity
 * @Author: shine
 * Date: 2018/12/24 下午1:10
 * Description: 重置密码 未使用
 * History:
 */

public class ResetPswActivity extends RxBaseActivity {
    @Override
    public int getLayoutId() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return R.layout.activity_set_psw;
    }

    LinearLayout accountCon;
    EditText editAccount;
    Button confirmAccount;

    LinearLayout pswCon;
    EditText editPsw;
    Button confirmPsw;

    LinearLayout authCodeCon;
    ImageView authImg;
    TextView clickRefresh;
    VerifyCodeView authInput;

    LinearLayout secCodeCon;
    TextView phoneNumber;
    TextView timerText;
    VerifyCodeView secInput;

    private String psw;

    private String authCode;

    private String phone;

    @Override
    public void initViews(Bundle savedInstanceState) {
        accountCon = findViewById(R.id.set_account_con);
        editAccount = findViewById(R.id.edit_account);
        confirmAccount = findViewById(R.id.confirm_account);

        pswCon = findViewById(R.id.set_psw_con);
        editPsw = findViewById(R.id.edit_psw);
        confirmPsw = findViewById(R.id.confirm_psw);

        authCodeCon = findViewById(R.id.edit_auth_code_code);
        authImg = findViewById(R.id.auth_img);
        clickRefresh = findViewById(R.id.click_refresh);
        authInput = findViewById(R.id.auth_input);

        secCodeCon = findViewById(R.id.edit_security_code_con);
        timerText = findViewById(R.id.timer_text);
        secInput = findViewById(R.id.sec_code_input);
        phoneNumber = findViewById(R.id.phone_number);

        String flag = getIntent().getStringExtra("flag");
        if (StringUtils.isNotBlank(flag) && flag.equals("doubleCheck")) {
            accountCon.setVisibility(View.GONE);
            authCodeCon.setVisibility(View.VISIBLE);

            phone = getIntent().getStringExtra("phone");
            psw = getIntent().getStringExtra("psw");

            getPicCode();
        }

        confirmAccount.setEnabled(false);
        editAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (StringUtils.isBlank(s.toString()) || s.toString().length() < 11) {
                    confirmAccount.setEnabled(false);
                    confirmAccount.setBackgroundDrawable(getResources().getDrawable(R.drawable.corners_button_press_bg));
                } else {
                    confirmAccount.setEnabled(true);
                    confirmAccount.setBackgroundDrawable(getResources().getDrawable(R.drawable.p_corners_button_bg));
                }
            }
        });

        confirmPsw.setEnabled(false);
        editPsw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (StringUtils.isBlank(s.toString()) || s.toString().length() < 6 || s.toString().length() > 16) {
                    confirmPsw.setEnabled(false);
                    confirmPsw.setBackgroundDrawable(getResources().getDrawable(R.drawable.corners_button_press_bg));
                } else {
                    confirmPsw.setEnabled(true);
                    confirmPsw.setBackgroundDrawable(getResources().getDrawable(R.drawable.p_corners_button_bg));
                }
            }
        });

        confirmAccount.setOnClickListener(v -> {
            phone = editAccount.getText().toString();
            PhoneUtil.hideKeyboard(ResetPswActivity.this);
            accountCon.setVisibility(View.GONE);
            pswCon.setVisibility(View.VISIBLE);
        });

        confirmPsw.setOnClickListener(v -> {
            psw = editPsw.getText().toString();
            PhoneUtil.hideKeyboard(ResetPswActivity.this);
            pswCon.setVisibility(View.GONE);
            authCodeCon.setVisibility(View.VISIBLE);

            getPicCode();
        });

        clickRefresh.setOnClickListener(v -> {
            getPicCode();
        });
        authInput.setOnCodeListener(code -> {
            checkCode(code.toLowerCase(),"pic");
        });

        timerText.setOnClickListener(v -> {
            getSmsCode();
        });
        secInput.setOnCodeListener(code -> {
            checkCode(code.toLowerCase(),"sms");
        });

    }

    /**
     * 重置密码
     */
    private void resetPsw() {
        McService api = ApiManager.getInstance().createApi(Config.HOST, McService.class);
        Observable<EmResult> observable = api
                .changePsw(phone, psw, EmUtil.getAppKey())
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<EmResult>(ResetPswActivity.this, true, true, new NoErrSubscriberListener<EmResult>() {
            @Override
            public void onNext(EmResult emResult) {
                ToastUtil.showMessage(ResetPswActivity.this, getString(R.string.reset_change_suc));
                ResetPswActivity.this.finish();
            }
        })));
    }

    /**
     * 返回结束本页面
     * @param view
     */
    public void backAction(View view) {
        finish();
    }

    /**
     * 定时器
     */
    private Timer timer;
    private TimerTask timerTask;
    /**
     * 验证码60秒倒计时
     */
    private int time = 60;

    /**
     * 初始化倒计时
     */
    private void initSecView() {
        phoneNumber.setText(phone);
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (time > 0) {
                    time--;
                    runOnUiThread(() -> {
                        timerText.setText(time + getString(R.string.reset_sec_resend));
                        timerText.setClickable(false);
                    });
                } else {
                    timer.cancel();
                    timerTask.cancel();
                    runOnUiThread(() -> {
                        timerText.setText(R.string.reset_resend_code);
                        timerText.setClickable(true);
                    });
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    @Override
    public boolean isEnableSwipe() {
        return true;
    }

    /**
     * 双因子获取图形验证码
     */
    private void getPicCode() {
        Observable<PicCodeResult> observable = ApiManager.getInstance().createApi(Config.HOST, McService.class)
                .picCode(phone, EmUtil.getAppKey())
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this,
                false,
                false,
                picCodeResult -> {
                    String picCode = picCodeResult.picCode;

                    authImg.setImageBitmap(CodeUtil.getInstance().createBitmap(picCode));
                    authCode = CodeUtil.getInstance().getCode();
                })));
    }

    /**
     * 检查code
     *
     * @param code
     * @param type code类型
     */
    private void checkCode(String code, String type) {
        Observable<EmResult> observable = ApiManager.getInstance().createApi(Config.HOST, McService.class)
                .checkCode(phone, code, type, EmUtil.getAppKey())
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this, false, false, new HaveErrSubscriberListener<EmResult>() {
            @Override
            public void onNext(EmResult result) {
                if (type.equals("pic")) {//图形验证码输入后
                    secCodeCon.setVisibility(View.VISIBLE);
                    authCodeCon.setVisibility(View.GONE);

                    getSmsCode();
                } else {
                    Intent intent = new Intent(ResetPswActivity.this, LoginActivity.class);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onError(int code) {
                authInput = new VerifyCodeView(ResetPswActivity.this);
                secInput = new VerifyCodeView(ResetPswActivity.this);
            }
        })));
    }

    /**
     * 获取短信验证码
     */
    private void getSmsCode() {
        Observable<EmResult> observable = ApiManager.getInstance().createApi(Config.HOST, McService.class)
                .smsCode(phone, EmUtil.getAppKey(), "中国", EmUtil.getEmployInfo().companyId)
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this,
                false,
                false,
                result -> {
                    initSecView();
                    ToastUtil.showMessage(ResetPswActivity.this, getString(R.string.get_sms_suc));
                })));
    }
}
