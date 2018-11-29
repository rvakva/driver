package cn.projcet.hf.securitycenter.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.projcet.hf.securitycenter.R;
import cn.projcet.hf.securitycenter.widget.CusToolbar;

/**
 * Created by developerLzh on 2017/5/4.
 */
public class WebActivity extends AppCompatActivity {

    private CusToolbar toolbar;

    public String  url;

    WebView webView;

    private ProgressBar myProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_descript);
        initToolbar();
        init();
        initWeb();
    }

    public void initToolbar() {
        toolbar = findViewById(R.id.cus_toolbar);
        toolbar.setLeftIcon(R.mipmap.ic_back, view -> {
            finish();
        });
        toolbar.setTitle("安全中心说明");
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
        webView.loadData("about:blank", "text/html", "UTF-8");
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

    public void init() {
        Intent localIntent = getIntent();
        url = localIntent.getStringExtra("url");
        if (url != null && !url.contains("http")) {
            url = "http://" + url;
        }
        webView = findViewById(R.id.web_view);
        myProgressBar = findViewById(R.id.myProgressBar);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWeb() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.loadUrl(url);


        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(url);
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

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    myProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    if (View.INVISIBLE == myProgressBar.getVisibility()) {
                        myProgressBar.setVisibility(View.VISIBLE);
                    }
                    myProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }


}
