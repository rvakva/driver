package com.easymi.zhuanche.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.easymi.component.utils.StringUtils;
import com.easymi.component.utils.ToastUtil;
import com.easymi.zhuanche.R;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName:RefuseOrderDialog
 * @Author: shine
 * Date: 2018/12/24 下午1:10
 * Description: 拒绝订单弹窗
 * History:
 */

public class RefuseOrderDialog extends Dialog {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setGravity(Gravity.CENTER);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        //设置dialog的宽度为当前手机屏幕的宽度
        p.width = d.getWidth();
        getWindow().setAttributes(p);
    }

    public RefuseOrderDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    /**
     * 构造器
     */
    public static class Builder {

        private Context context;
        CheckBox reason_1;
        CheckBox reason_2;
        CheckBox reason_3;
        CheckBox reason_4;
        EditText edit_reason;
        Button refuse_apply;
        ImageView ic_close;

        RefuseOrderDialog dialog;

        /**
         * 拒绝原因
         */
        private String reason;

        private OnApplyClick applyClick;

        public void setApplyClick(OnApplyClick applyClick) {
            this.applyClick = applyClick;
        }

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 初始化布局
         * @return
         */
        public RefuseOrderDialog create() {
            dialog = new RefuseOrderDialog(context, R.style.Dialog);
            View view = LayoutInflater.from(context).inflate(R.layout.zc_refuse_dialog, null, true);
            ic_close = view.findViewById(R.id.ic_close);
            reason_1 = view.findViewById(R.id.reason_1);
            reason_2 = view.findViewById(R.id.reason_2);
            reason_3 = view.findViewById(R.id.reason_3);
            reason_4 = view.findViewById(R.id.reason_4);
            edit_reason = view.findViewById(R.id.edit_reason);
            refuse_apply = view.findViewById(R.id.refuse_apply);

            ic_close.setOnClickListener(view1 -> dialog.dismiss());

            reason_1.setOnClickListener(new CusClickListener(reason_1));
            reason_2.setOnClickListener(new CusClickListener(reason_2));
            reason_3.setOnClickListener(new CusClickListener(reason_3));
            reason_4.setOnClickListener(new CusClickListener(reason_4));

            edit_reason.setOnFocusChangeListener((view12, b) -> {
                if (b) {
                    resetAllCheck();
                }
            });

            edit_reason.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    reason = editable.toString();
                }
            });

            refuse_apply.setOnClickListener(view13 -> {
                if (null != applyClick) {
                    if (StringUtils.isNotBlank(reason)) {
                        dialog.dismiss();
                        applyClick.onApplyClick(reason);
                    } else {
                        ToastUtil.showMessage(context, context.getString(R.string.not_non_refuse));
                    }
                }
            });

            dialog.setContentView(view);
            return dialog;
        }

        /**
         * 重制选择框
         */
        private void resetAllCheck() {
            reason_1.setChecked(false);
            reason_2.setChecked(false);
            reason_3.setChecked(false);
            reason_4.setChecked(false);
        }

        /**
         * 自定义点击接口实现
         */
        class CusClickListener implements View.OnClickListener {
            CheckBox checkBox;

            public CusClickListener(CheckBox checkBox) {
                this.checkBox = checkBox;
            }

            @Override
            public void onClick(View view) {
                resetAllCheck();
                checkBox.setChecked(true);
                reason = checkBox.getText().toString();
            }
        }
    }

    /**
     * 点击拒绝订单接口
     */
    public interface OnApplyClick {
        /**
         * 点击拒绝监听
         * @param reason
         */
        void onApplyClick(String reason);
    }

}
