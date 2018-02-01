package com.easymi.personal.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.easymi.component.Config;
import com.easymi.component.app.ActManager;
import com.easymi.component.app.XApp;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.loc.LocService;
import com.easymi.component.loc.LocationHelperService;
import com.easymi.component.widget.CusToolbar;
import com.easymi.component.widget.switchButton.SwitchButton;
import com.easymi.personal.R;

/**
 * Created by developerLzh on 2017/11/6 0006.
 */
@Route(path = "/personal/SetActivity")
public class SetActivity extends RxBaseActivity {

    SwitchButton voiceAble;
    SwitchButton shakeAble;
    SwitchButton alwaysOren;
    SwitchButton defaultNavi;

    @Override
    public int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        voiceAble = findViewById(R.id.voice_able_btn);
        shakeAble = findViewById(R.id.shake_btn);
        alwaysOren = findViewById(R.id.oren_btn);
        defaultNavi = findViewById(R.id.default_navi);

        voiceAble.setChecked(XApp.getMyPreferences().getBoolean(Config.SP_VOICE_ABLE, true));
        shakeAble.setChecked(XApp.getMyPreferences().getBoolean(Config.SP_SHAKE_ABLE, true));
        alwaysOren.setChecked(XApp.getMyPreferences().getBoolean(Config.SP_ALWAYS_OREN, false));
        defaultNavi.setChecked(XApp.getMyPreferences().getBoolean(Config.SP_DEFAULT_NAVI, true));

        initSwitch();
    }

    private void initSwitch() {
        voiceAble.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = XApp.getPreferencesEditor();
            editor.putBoolean(Config.SP_VOICE_ABLE, isChecked);
            editor.apply();
        });
        shakeAble.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = XApp.getPreferencesEditor();
            editor.putBoolean(Config.SP_SHAKE_ABLE, isChecked);
            editor.apply();
        });
        alwaysOren.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = XApp.getPreferencesEditor();
            editor.putBoolean(Config.SP_ALWAYS_OREN, isChecked);
            editor.apply();
        });
        defaultNavi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = XApp.getPreferencesEditor();
            editor.putBoolean(Config.SP_DEFAULT_NAVI, isChecked);
            editor.apply();
        });
    }

    @Override
    public void initToolBar() {
        CusToolbar cusToolbar = findViewById(R.id.cus_toolbar);
        cusToolbar.setOnClickListener(v -> finish());
        cusToolbar.setTitle(R.string.person_set);
    }

    public void changePsw(View view) {
        Intent intent = new Intent(this, ChangeActivity.class);
        startActivity(intent);
    }

    public void choiceLanguage(View view) {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivity(intent);
    }

    public void toStats(View view) {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    public void helpCenter(View view) {
        Intent intent = new Intent(SetActivity.this, ArticleActivity.class);
        intent.putExtra("tag", "ContactUs");
        intent.putExtra("title", getString(R.string.set_help));
        startActivity(intent);
    }

    public void feedBack(View view) {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }

    public void naviPrefence(View view) {
        Intent intent = new Intent(this, NaviSetActivity.class);
        startActivity(intent);
    }

    public void contractUs(View view) {
        Intent intent = new Intent(SetActivity.this, ArticleActivity.class);
        intent.putExtra("tag", "ContactUs");
        intent.putExtra("title", getString(R.string.set_contract_us));
        startActivity(intent);
    }

    public void aboutUs(View view) {
        Intent intent = new Intent(SetActivity.this, ArticleActivity.class);
        intent.putExtra("tag", "AboutUs");
        intent.putExtra("title", getString(R.string.set_about_us));
        startActivity(intent);
    }

    public void logOut(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.set_hint))
                .setMessage(getString(R.string.set_sure_exit))
                .setPositiveButton(getString(R.string.set_sure), (dialogInterface, i) -> {
                    employLogout();
                })
                .setNegativeButton(getString(R.string.set_cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .create();
        dialog.show();

    }

    private void employLogout() {
        SharedPreferences.Editor editor = XApp.getPreferencesEditor();
        editor.putBoolean(Config.SP_ISLOGIN, false);
        editor.putLong(Config.SP_DRIVERID, -1);
        editor.apply();

        stopAllService(this);
        ActManager.getInstance().finishAllActivity();

        Intent i = getPackageManager()
                .getLaunchIntentForPackage(getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        ActivityManager activityMgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityMgr != null) {
            activityMgr.killBackgroundProcesses(getPackageName());
        }
        System.exit(0);
    }

    private void stopAllService(Context context) {

        Intent locIntent = new Intent(context, LocService.class);
        context.stopService(locIntent);

        Intent locHelpIntent = new Intent(context, LocationHelperService.class);
        context.stopService(locHelpIntent);
        try {
            Service mQTTService = (Service) (Class.forName("com.easymi.common.push.MQTTService").newInstance());
            Service jobKeepLiveService = (Service) Class.forName("com.easymi.common.daemon.JobKeepLiveService").newInstance();
            Service puppetService = (Service) Class.forName("com.easymi.common.daemon.PuppetService").newInstance();
            Service daemonService = (Service) Class.forName("com.easymi.common.daemon.DaemonService").newInstance();

            Intent mqttIntent = new Intent(context, mQTTService.getClass());
            mqttIntent.setPackage(getPackageName());
            context.stopService(mqttIntent);

            Intent daemonIntent = new Intent(context, daemonService.getClass());
            daemonIntent.setPackage(getPackageName());
            context.stopService(daemonIntent);

            Intent jobkeepIntent = new Intent(context, jobKeepLiveService.getClass());
            jobkeepIntent.setPackage(getPackageName());
            context.stopService(jobkeepIntent);

            Intent puppetIntent = new Intent(context, puppetService.getClass());
            puppetIntent.setPackage(getPackageName());
            context.stopService(puppetIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
