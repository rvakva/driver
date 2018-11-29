package cn.projcet.hf.securitycenter.network;

import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.util.Log;

import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import cn.projcet.hf.securitycenter.R;
import cn.projcet.hf.securitycenter.utils.ToastUtil;
import cn.projcet.hf.securitycenter.widget.LoadingButton;
import retrofit2.HttpException;
import rx.Subscriber;

/**
 * Created by Administrator on 2016/9/26.
 */

public class MySubscriber<T> extends Subscriber<T> implements ProgressDismissListener {

    private Context context;

    private ProgressHandler progressHandler;
    private LoadingBtnHandler loadingBtnHandler;

    private NoErrSubscriberListener<T> noErrSubscriberListener;
    private HaveErrSubscriberListener<T> haveErrSubscriberListener;

    /**
     * @param needShowProgress        是否显示加载框
     * @param dialogCancelable        加载框是否可以取消
     * @param noErrSubscriberListener 不需要错误码的回调事件
     */
    public MySubscriber(
            Context context,
            boolean needShowProgress,
            boolean dialogCancelable,
            NoErrSubscriberListener<T> noErrSubscriberListener) {
        this.context = context;
        this.noErrSubscriberListener = noErrSubscriberListener;
        if (needShowProgress) {
            progressHandler = new ProgressHandler(context, dialogCancelable, this);
        }
    }

    /**
     * @param needShowProgress          是否显示加载框
     * @param dialogCancelable          加载框是否可以取消
     * @param haveErrSubscriberListener 需要错误码回调的事件
     */
    public MySubscriber(
            Context context,
            boolean needShowProgress,
            boolean dialogCancelable,
            HaveErrSubscriberListener<T> haveErrSubscriberListener) {
        this.context = context;
        this.haveErrSubscriberListener = haveErrSubscriberListener;
        if (needShowProgress) {
            progressHandler = new ProgressHandler(context, dialogCancelable, this);
        }
    }

    /**
     * @param context
     * @param button                    加载的按钮
     * @param haveErrSubscriberListener
     */
    public MySubscriber(Context context, LoadingButton button, HaveErrSubscriberListener<T> haveErrSubscriberListener) {
        this.context = context;
        this.haveErrSubscriberListener = haveErrSubscriberListener;
        loadingBtnHandler = new LoadingBtnHandler(button, this);
    }

    /**
     * @param context
     * @param button                  加载的按钮
     * @param noErrSubscriberListener
     */
    public MySubscriber(Context context, LoadingButton button, NoErrSubscriberListener<T> noErrSubscriberListener) {
        this.context = context;
        this.noErrSubscriberListener = noErrSubscriberListener;
        loadingBtnHandler = new LoadingBtnHandler(button, this);
    }

    @Override
    public void onCompleted() {
        Log.e("MySubscriber", "mission complete");

        if (null != progressHandler) {
            progressHandler.sendEmptyMessage(ProgressHandler.DISMISS_DIALOG);
        } else if (null != loadingBtnHandler) {
            loadingBtnHandler.sendEmptyMessage(LoadingBtnHandler.HIDE_BTN_LOADING);
        } else {
            this.onProgressDismiss();
        }

        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }

    }

    /**
     * 处理错误信息
     */
    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof HttpException) {
            ToastUtil.showMessage(context, context.getString(R.string.response_error) + ((HttpException) e).code());//400、500、404之类的响应码错误
        } else if (e instanceof SocketTimeoutException || e instanceof SocketException) {
            ToastUtil.showMessage(context, context.getString(R.string.out_time));//连接超时错误
        } else if (e instanceof ConnectException) {
            ToastUtil.showMessage(context, context.getString(R.string.no_internet));//连接失败错误
        } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException) {
            ToastUtil.showMessage(context, context.getString(R.string.parse_error));//解析错误
        }
        if (null != progressHandler) {
            progressHandler.sendEmptyMessage(ProgressHandler.DISMISS_DIALOG);
        } else if (null != loadingBtnHandler) {
            loadingBtnHandler.sendEmptyMessage(LoadingBtnHandler.HIDE_BTN_LOADING);
        } else {
            this.onProgressDismiss();
        }

        if (!this.isUnsubscribed()) {
            this.unsubscribe();
        }
    }

    @Override
    public void onNext(T t) {
        if (haveErrSubscriberListener != null) {
            haveErrSubscriberListener.onNext(t);
        }
        if (noErrSubscriberListener != null) {
            noErrSubscriberListener.onNext(t);
        }
    }

    /**
     * 在开始订阅时，如果需要显示加载框
     */
    @Override
    public void onStart() {
        super.onStart();
        if (null != progressHandler) {
            progressHandler.sendEmptyMessage(ProgressHandler.SHOW_DIALOG);
        } else if (null != loadingBtnHandler) {
            loadingBtnHandler.sendEmptyMessage(LoadingBtnHandler.SHOW_BTN_LOADING);
        }
    }

    /**
     * 在加载框消失时是整个流程的最后一步
     * 好像可以防止内存泄露
     */
    @Override
    public void onProgressDismiss() {

    }
}
