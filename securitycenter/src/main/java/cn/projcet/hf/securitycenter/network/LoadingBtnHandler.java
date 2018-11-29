package cn.projcet.hf.securitycenter.network;

import android.os.Handler;
import android.os.Message;


import cn.projcet.hf.securitycenter.widget.LoadingButton;

/**
 * Created by liuzihao on 2017/12/11.
 */

public class LoadingBtnHandler extends Handler {

    LoadingButton loadingButton;

    public static final int SHOW_BTN_LOADING = 0X03;
    public static final int HIDE_BTN_LOADING = 0X04;

    private ProgressDismissListener progressDismissListener;

    public LoadingBtnHandler(LoadingButton loadingButton, ProgressDismissListener listener) {
        this.loadingButton = loadingButton;
        this.progressDismissListener = listener;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case SHOW_BTN_LOADING:
                loadingButton.setClickable(false);
                loadingButton.setStatus(LoadingButton.STATUS_LOADING);
                break;

            case HIDE_BTN_LOADING:
                loadingButton.setClickable(true);
                loadingButton.setStatus(LoadingButton.STATUS_NORMAL);
                if (null != progressDismissListener) {
                    progressDismissListener.onProgressDismiss();
                }
                break;
        }
    }
}
