package com.easymi.common.faceCheck;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.easymi.common.CommApiService;
import com.easymi.common.R;
import com.easymi.common.entity.FaceComparResult;
import com.easymi.common.entity.FaceConfig;
import com.easymi.common.entity.Pic;
import com.easymi.common.entity.QiNiuToken;
import com.easymi.common.widget.CommonDialog;
import com.easymi.component.Config;
import com.easymi.component.base.RxBaseActivity;
import com.easymi.component.entity.BaseOrder;
import com.easymi.component.entity.EmLoc;
import com.easymi.component.network.ApiManager;
import com.easymi.component.network.HaveErrSubscriberListener;
import com.easymi.component.network.HttpResultFunc;
import com.easymi.component.network.HttpResultFunc2;
import com.easymi.component.network.MySubscriber;
import com.easymi.component.result.EmResult;
import com.easymi.component.utils.CsSharedPreferences;
import com.easymi.component.utils.DensityUtil;
import com.easymi.component.utils.EmUtil;
import com.easymi.component.utils.TimeUtil;
import com.easymi.component.utils.ToastUtil;
import com.easymi.component.widget.CusToolbar;
import com.easymi.component.widget.RxProgressHUD;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.easymi.component.Config.FT_ORIENT;

/**
 * @Copyright (C), 2012-2019, Sichuan Xiaoka Technology Co., Ltd.
 * @FileName: RegisterAndRecognizeActivity
 * @Author: hufeng
 * @Date: 2019/11/13 上午10:51
 * @Description:
 * @History:
 */
public class RegisterAndRecognizeActivity extends RxBaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = "hufeng";

    RoundTextureView texture_preview;
    TextView tv_hint;
    CusToolbar cusToolbar;

    TextView tv_name_hint;
    TextView tv_name;

//    RoundBorderView roundBorderView;

    private static final int MAX_DETECT_NUM = 10;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    /**
     * 0 认证 1 对比
     */
    int flag = 0;

    /**
     * 优先打开的摄像头，本界面主要用于单目RGB摄像头设备，因此默认打开前置
     */
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;

    /**
     * 相机使用帮助类
     */
    private CameraHelper cameraHelper;

    /**
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;

    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
    /**
     * 出错重试最大次数
     */
    private static final int MAX_RETRY_TIME = 3;

    private Camera.Size previewSize;

    private FaceHelper faceHelper;

    @Override
    public void initToolBar() {
        cusToolbar = findViewById(R.id.cus_toolbar);
        cusToolbar.setLeftBack(view -> finish());
        if (flag == 0) {
            cusToolbar.setTitle("刷脸认证");
        } else {
            cusToolbar.setTitle("刷脸识别");
        }
    }

    @Override
    public boolean isEnableSwipe() {
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_register_and_recognize;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        texture_preview = findViewById(R.id.texture_preview);
        tv_hint = findViewById(R.id.tv_hint);
        tv_name = findViewById(R.id.tv_name);
        tv_name_hint = findViewById(R.id.tv_name_hint);


        //在布局结束后才做初始化操作
        texture_preview.getViewTreeObserver().addOnGlobalLayoutListener(this);

        flag = getIntent().getIntExtra("flag", 0);

        if (flag == 0) {
            String name = getIntent().getStringExtra("name");

            if (!TextUtils.isEmpty(name)) {
                tv_name.setText("*" + name.substring(1, name.length()));
                tv_name.setVisibility(View.VISIBLE);
                tv_name_hint.setVisibility(View.VISIBLE);
            }
        } else {
            tv_name.setVisibility(View.INVISIBLE);
            tv_name_hint.setVisibility(View.INVISIBLE);
        }

        getQiniuToken();
    }

    FaceEngine faceEngine = new FaceEngine();

    /**
     * 激活引擎
     */
    public void activeEngine() {
        Log.e(TAG, "faceEngine" + (faceEngine == null));
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            int activeCode = faceEngine.activeOnline(RegisterAndRecognizeActivity.this, "DdU5KdD96mNGpq949QLxzxa5nFvQoeVBnGkvdi1rXCfY", "AeP1rPdQvo1bY1uL2H8mvPjofGfFwT2D5bR2iqPWH2L7");
            emitter.onNext(activeCode);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            Log.e(TAG, "引擎激活成功");
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            Log.e(TAG, "引擎已激活，无需再次激活");
                        } else {
                            Log.e(TAG, "引擎激活失败，错误码为:" + activeCode);
                        }

                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = faceEngine.getActiveFileInfo(getBaseContext(), activeFileInfo);
                        if (res == ErrorInfo.MOK || res == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            initEngine();
                            initCamera();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }



    /**
     * 倒计时计时器
     */
    Timer timer;
    TimerTask timerTask;

    /**
     * 取消定时器
     */
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    int timeSeq = -1;

    public void setCountTIme(){
        timeSeq = 2;

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
                timeSeq--;
                if (timeSeq == 0){
                    cancelTimer();
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }





    /**
     * 在{@link #texture_preview}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        texture_preview.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        ViewGroup.LayoutParams layoutParams = texture_preview.getLayoutParams();
        int sideLength = Math.min(texture_preview.getWidth(), texture_preview.getHeight()) * 3 / 5;
        layoutParams.width = sideLength;
        layoutParams.height = sideLength;
        texture_preview.setLayoutParams(layoutParams);

        texture_preview.setRadius(Math.min(texture_preview.getWidth(), texture_preview.getHeight()) / 2);
        texture_preview.turnRound();

        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            activeEngine();
        }
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine();
            } else {
                ToastUtil.showMessage(this, "权限被拒绝！");
            }
        }
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, getFtOrient(),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(this, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(this, FaceEngine.ASF_DETECT_MODE_IMAGE, FaceEngine.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);

        VersionInfo versionInfo = new VersionInfo();
        ftEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + ftInitCode + "  version:" + versionInfo);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = "ftEngine初始化失败，错误码为:" + ftInitCode;
            Log.i(TAG, "initEngine: " + error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = "frEngine初始化失败，错误码为:%d" + frInitCode;
            Log.i(TAG, "initEngine: " + error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = "flEngine初始化失败，错误码为:%d" + flInitCode;
            Log.i(TAG, "initEngine: " + error);
        }
    }

    public static int getFtOrient() {
        return CsSharedPreferences.getInstance().getInt(FT_ORIENT, FaceEngine.ASF_OP_270_ONLY);
    }

    UpLoadProgressDialog dialog;

    /**
     * 初始化相机
     */
    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                Log.i(TAG, "onFaceFeatureInfoGet: ");
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {

                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    livenessMap.put(requestId, liveness);
                    // 非活体，重试
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        retryLivenessDetectDelayed(requestId);
                        Log.e(TAG, "非活体");
                    } else if (liveness == LivenessInfo.ALIVE) {
                        Log.e(TAG, "成功");
                        cameraHelper.take();
                        runOnUiThread(() -> {
                            dialog = new UpLoadProgressDialog(RegisterAndRecognizeActivity.this);
                            dialog.show();
                            dialog.setProgress("信息获取中...", 22);
                        });
                    } else if (liveness == LivenessInfo.FACE_NUM_MORE_THAN_ONE) {
                        Log.e(TAG, "不止一个");
                    } else if (liveness == LivenessInfo.UNKNOWN) {
                        Log.e(TAG, "未知");
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }
        };


        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: ");
                Camera.Size lastPreviewSize = previewSize;
                previewSize = camera.getParameters().getPreviewSize();
                // 切换相机的时候可能会导致预览尺寸发生变化
                if (faceHelper == null || lastPreviewSize == null || lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
                    if (faceHelper != null) {
                        faceHelper.release();
                    }
                    faceHelper = new FaceHelper.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
                            .frQueueSize(MAX_DETECT_NUM)
                            .flQueueSize(MAX_DETECT_NUM)
                            .previewSize(previewSize)
                            .faceListener(faceListener)
                            .build();
                }
                setCountTIme();
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (timeSeq == 0){
                    List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                    if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                        for (int i = 0; i < facePreviewInfoList.size(); i++) {
                            Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                            /**
                             * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                             */
                            if ((status == null || status != RequestFeatureStatus.SUCCEED)) {
                                Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                                Log.e(TAG, "liveness:" + liveness);
                                if (liveness == null || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                                    livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                                    faceHelper.requestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        /**
         * 图片保存成功后的回调
         */
        PictureListener pictureListener = picturePath -> {
            runOnUiThread(() -> {
                if (dialog != null) {
                    dialog.setProgress("信息上传中...", 51);
                }
            });
            updateImage(new File(picturePath));
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(texture_preview.getMeasuredWidth(), texture_preview.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(texture_preview)
                .cameraListener(cameraListener)
                .pictureListener(pictureListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }


    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    @Override
    protected void onDestroy() {

        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        unInitEngine();
        if (faceHelper != null) {
            faceHelper.release();
            faceHelper = null;
        }

        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }

        super.onDestroy();
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }


    /**
     * 图片上传七牛云token
     */
    String qiniuToken;

    /**
     * 认证或者对比图片
     */
    String faceAuthPic;

    /**
     * 获取七牛云token
     */
    public void getQiniuToken() {
        rx.Observable<QiNiuToken> observable = ApiManager.getInstance().createApi(Config.HOST, CommApiService.class)
                .getToken()
                .subscribeOn(rx.schedulers.Schedulers.io())
                .filter(new HttpResultFunc<>())
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread());
        ;
        mRxManager.add(observable.subscribe(new MySubscriber<>(this, false, false, qiNiuToken -> {
            if (qiNiuToken.getCode() == 1) {
                qiniuToken = qiNiuToken.qiNiu;
                Log.e(TAG, "qiniuToken:" + qiniuToken);
                if (qiniuToken == null) {
                    throw new IllegalArgumentException("token无效");
                }
            }
        })));
    }


    /**
     * 上传图片
     *
     * @param file
     */
    public void updateImage(File file) {
        if (TextUtils.isEmpty(qiniuToken)) {
            ToastUtil.showMessage(this, "配置获取错误，请稍后重试");
            return;
        }
        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/jpg"), file);
        RequestBody tokenBody = RequestBody.create(MediaType.parse("multipart/form-data"), qiniuToken);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), photoRequestBody);

        rx.Observable<Pic> observable = ApiManager.getInstance().createApi(Config.HOST, CommApiService.class)
                .uploadPic(Config.HOST_UP_PIC, tokenBody, body)
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread());
        mRxManager.add(observable.subscribe(new MySubscriber<>(this, false, false, pic -> {
            faceAuthPic = pic.hashCode;
            if (flag == 0) {
                if (dialog != null) {
                    dialog.setProgress("认证中...", 76);
                }
                faceAuth(faceAuthPic);
            } else {
                if (dialog != null) {
                    dialog.setProgress("信息比对中...", 76);
                }
                faceCompar(faceAuthPic);
            }
        })));
    }

    /**
     * 网络超时，请检查网络
     */
    int FACE_TIME_OUT_ERR = 40703;

    /**
     * 人脸认证
     *
     * @param picPath
     */
    public void faceAuth(String picPath) {
        EmLoc emLoc = EmUtil.getLastLoc();
        rx.Observable<EmResult> observable = ApiManager.getInstance().createApi(Config.HOST, CommApiService.class)
                .faceAuth(picPath, emLoc.address, emLoc.longitude, emLoc.latitude)
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this, false, false, emResult -> {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (emResult.getCode() == 1) {
                ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "认证成功");
                setResult(RESULT_OK);
                finish();
            } else if (emResult.getCode() == FACE_TIME_OUT_ERR) {
                ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "网络超时，请检查网络");
                cameraHelper.startPreview();
                setCountTIme();
            } else {
                ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "认证失败，请重试");
                cameraHelper.startPreview();
                setCountTIme();
            }
        })));
    }

    /**
     * 人脸识别
     *
     * @param picPath
     */
    public void faceCompar(String picPath) {
        EmLoc emLoc = EmUtil.getLastLoc();
        rx.Observable<FaceComparResult> observable = ApiManager.getInstance().createApi(Config.HOST, CommApiService.class)
                .faceCompar(picPath, emLoc.address, emLoc.longitude, emLoc.latitude, "DRIVER_ONLINE")
                .filter(new HttpResultFunc<>())
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread());

        mRxManager.add(observable.subscribe(new MySubscriber<>(this, false, false, emResult -> {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (emResult.getCode() == 1) {
                if (emResult.data.identical) {
                    ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "识别成功");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "识别失败，请按照提示操作");
                    cameraHelper.startPreview();
                    setCountTIme();
                }
            } else if (emResult.getCode() == FACE_TIME_OUT_ERR) {
                ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "网络超时，请检查网络");
                cameraHelper.startPreview();
                setCountTIme();
            } else {
                ToastUtil.showMessage(RegisterAndRecognizeActivity.this, "识别失败，请重试");
                cameraHelper.startPreview();
                setCountTIme();
            }
        })));
    }
}