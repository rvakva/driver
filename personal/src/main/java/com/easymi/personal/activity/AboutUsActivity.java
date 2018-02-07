package com.easymi.personal.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.easymi.component.Config;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.network.ApiManager;
import com.easymi.component.network.HttpResultFunc;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.network.NoErrSubscriberListener;
import com.easymi.component.utils.PhoneUtil;
import com.easymi.component.utils.StringUtils;
import com.easymi.component.widget.CusToolbar;
import com.easymi.personal.McService;
import com.easymi.personal.R;
import com.easymi.personal.result.ArticleResult;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by liuzihao on 2018/2/7.
 */

public class AboutUsActivity extends RxBaseActivity {

    private TextView webSiteText;
    private TextView phoneText;
    private WebView webView;
    private TextView versionText;

    private CusToolbar cusToolbar;

    @Override
    public int getLayoutId() {
        return R.layout.activity_about_us;
    }

    @Override
    public void initToolBar() {
        super.initToolBar();
        cusToolbar = findViewById(R.id.cus_toolbar);
        cusToolbar.setLeftBack(view -> finish());
        cusToolbar.setTitle(R.string.set_about_us);
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        webSiteText = findViewById(R.id.website);
        phoneText = findViewById(R.id.phone);
        webView = findViewById(R.id.web_view);
        versionText = findViewById(R.id.version);
        getArticle();
        try {
            versionText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        phoneText.setOnClickListener(view -> {
            if (StringUtils.isNotBlank(phoneText.getText().toString())) {
                PhoneUtil.call(AboutUsActivity.this, phoneText.getText().toString());
            }
        });

        webSiteText.setOnClickListener(view -> {
            if (StringUtils.isNotBlank(webSiteText.getText().toString())) {
                Uri uri = Uri.parse(webSiteText.getText().toString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        initWeb();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWeb() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });

    }

    private void getArticle(String alias) {
        McService api = ApiManager.getInstance().createApi(Config.HOST, McService.class);

        Observable<ArticleResult> observable = api
                .getArticle(Config.APP_KEY, alias)
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this, true,
                true, emResult -> webView.loadData(emResult.article.contents, "text/html;charset=UTF-8", null))));
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        webView.loadData("about:blank", "text/html", "UTF-8");
        webView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        webView.clearCache(true);
        webView.destroy();
        super.onDestroy();
    }

    private void getArticle() {
        McService api = ApiManager.getInstance().createApi(Config.HOST, McService.class);

        Observable<ArticleResult> observable = api
                .getArticle(Config.APP_KEY, "AboutUs")
                .filter(new HttpResultFunc<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this, true,
                true, articleResult -> {
            webView.loadData(articleResult.article.contents, "text/html;charset=UTF-8", null);
            phoneText.setText(articleResult.article.phone);
            webSiteText.setText(articleResult.article.url);
        }
        )));
    }
}
