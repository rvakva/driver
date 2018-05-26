package com.easymi.common.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.easymi.common.R;
import com.easymi.common.mvp.work.WorkActivity;
import com.easymi.component.Config;
import com.easymi.component.app.ActManager;
import com.easymi.component.app.XApp;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.permission.RxPermissions;
import com.easymi.component.update.UpdateHelper;
import com.easymi.component.utils.Log;
import com.easymi.component.utils.StringUtils;

import java.io.IOException;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by developerLzh on 2017/11/3 0003.
 */
@Route(path = "/common/SplashActivity")
public class SplashActivity extends RxBaseActivity {
    @Override
    public boolean isEnableSwipe() {
        return false;
    }

    @Override
    public int getLayoutId() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return R.layout.activity_splash;
    }

    RxPermissions rxPermissions;

    GifDrawable gifFromAssets;

    TextView jumpOver;

    private int leftTime;

    private static final String TAG = "SplashActivity";

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (leftTime > 0) {
                        leftTime--;
                        jumpOver.setText(getString(R.string.jump_gif) + "(" + leftTime + getString(R.string.sec) + ")");
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animateStarted) {
            handler.removeMessages(0);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (animateStarted) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    }


    private boolean animateStarted = false;

    @Override
    public void initViews(Bundle savedInstanceState) {
        Log.e(TAG, "initViews");
        jumpOver = findViewById(R.id.jump_over);

        rxPermissions = new RxPermissions(this);

        loadLanguage();

        GifImageView view = findViewById(R.id.splash);
        try {
            Log.e(TAG, "try");
            if(!Config.COMM_USE){
                gifFromAssets = new GifDrawable(getAssets(), "splash_gif.gif");
                view.setBackground(gifFromAssets);
                gifFromAssets.pause();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || !rxPermissions.isGranted(Manifest.permission.READ_PHONE_STATE)
                    || !rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e(TAG, "showDialog");
                showDialog();
            } else {
                Log.e(TAG, "checkForUpdate");
                checkForUpdate();
            }
        }
    }

    private void delayIn() {
        if (needShowAnimate()) {
            XApp.getPreferencesEditor().putLong(Config.SP_LAST_SPLASH_TIME, System.currentTimeMillis()).apply();
            if (null != gifFromAssets) {
                Log.e(TAG, "null != gifFromAssets");
                leftTime = gifFromAssets.getDuration() / 1000;
                jumpOver.setText(getString(R.string.jump_gif) + "(" + leftTime + getString(R.string.sec) + ")");
                gifFromAssets.start();
                animateStarted = true;
                gifFromAssets.addAnimationListener(loopNumber -> jump());
                handler.sendEmptyMessageDelayed(0, 1000);
            } else {
                Log.e(TAG, "null == gifFromAssets");
                handler.postDelayed(this::jump, 2000);
            }
        } else {
            Log.e(TAG, "! needShowAnimate");
            jump();
        }
    }

    /**
     * 是否显示首页动画
     *
     * @return
     */
    private boolean needShowAnimate() {
        long lastShowAnimaTime = XApp.getMyPreferences().getLong(Config.SP_LAST_SPLASH_TIME, 0);
        if (lastShowAnimaTime == 0) {
            return true;
        } else {
            if (System.currentTimeMillis() - lastShowAnimaTime > 24 * 60 * 60 * 1000) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @param view
     */
    public void jumpOver(View view) {
        jump();
    }

    /**
     * 检查更新
     */
    private void checkForUpdate() {
        new UpdateHelper(this, new UpdateHelper.OnNextListener() {
            @Override
            public void onNext() {
                Log.e(TAG, "onNext");
                runOnUiThread(() -> delayIn());
            }

            @Override
            public void onNoVersion() {
                Log.e(TAG, "onNoVersion");
                runOnUiThread(() -> delayIn());
            }
        });
    }

    private void jump() {
        boolean isLogin = XApp.getMyPreferences().getBoolean(Config.SP_ISLOGIN, false);
        if (isLogin) {
            startActivity(new Intent(SplashActivity.this, WorkActivity.class));
        } else {
            ARouter.getInstance()
                    .build("/personal/LoginActivity")
                    .navigation();
        }
        finish();
    }

    private void delayExit() {
        handler.postDelayed(() -> runOnUiThread(() -> {
            ActManager.getInstance().finishAllActivity();
        }), 1000);
    }


    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.hint))
                .setMessage(getString(R.string.message))
                .setPositiveButton(getString(R.string.sure), (dialog1, which) -> requestPer())
                .setCancelable(false)
                .create();
        dialog.show();
    }

    private void requestPer() {
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        checkForUpdate();
                    } else {
                        Toast.makeText(SplashActivity.this, getString(R.string.exit), Toast.LENGTH_SHORT).show();
                        delayExit();
                    }
                });
    }

    /**
     * 加载本地设置的语言
     */
    private void loadLanguage() {
        Log.e(TAG, "loadLanguage");
        SharedPreferences preferences = XApp.getMyPreferences();

        Configuration config = getResources().getConfiguration();   //获取默认配置
        int language = preferences.getInt(Config.SP_USER_LANGUAGE, Config.SP_LANGUAGE_AUTO);
        switch (language) {
            case Config.SP_SIMPLIFIED_CHINESE:
                config.locale = Locale.SIMPLIFIED_CHINESE;  //加载简体中文
                break;

            case Config.SP_TRADITIONAL_CHINESE:
                config.locale = Locale.TRADITIONAL_CHINESE;  //加载台湾繁体
                break;

            case Config.SP_LANGUAGE_AUTO:
                String sysLan = preferences.getString(Config.SP_SYS_LANGUAGE, "");
                if (StringUtils.isBlank(sysLan)) {
                    preferences.edit().putString(Config.SP_SYS_LANGUAGE,
                            Locale.getDefault().toString()).apply();
                } else {
                    if (sysLan.contains(Locale.TAIWAN.toString())
                            || sysLan.contains(Locale.TRADITIONAL_CHINESE.toString())) {
                        config.locale = Locale.TRADITIONAL_CHINESE;
                    } else if (sysLan.contains("en")) {
                        config.locale = Locale.ENGLISH;
                    } else {
                        config.locale = Locale.SIMPLIFIED_CHINESE;
                    }
                }
                break;

            case Config.SP_ENGLISH:
                config.locale = Locale.ENGLISH;    //获取默认区域
                break;
        }
        getBaseContext().getResources().updateConfiguration(config, null);   //更新配置文件
    }
}
