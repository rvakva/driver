package com.easymi.component;

import com.easymi.component.app.XApp;

/**
 * @Copyright (C), 2012-2019, Sichuan Xiaoka Technology Co., Ltd.
 * @FileName: GWOrderStatus
 * @Author: hufeng
 * @Date: 2019/3/27 上午11:11
 * @Description:
 * @History:
 */
public class GWOrderStatus {
    /**
     * 新单
     */
    public static final int NEW_ORDER = 1;
    /**
     * 已派单
     */
    public static final int PAIDAN_ORDER = 5;
    /**
     * 已接单
     */
    public static final int TAKE_ORDER = 10;
    /**
     * 前往预约地
     */
    public static final int GOTO_BOOKPALCE_ORDER = 15;
    /**
     * 到达预约地
     */
    public static final int ARRIVAL_BOOKPLACE_ORDER = 20;
    /**
     * 前往目的地
     */
    public static final int GOTO_DESTINATION_ORDER = 25;
    /**
     * 中途等待
     */
    public static final int START_WAIT_ORDER = 28;
    /**
     * 到达目的地
     */
    public static final int ARRIVAL_DESTINATION_ORDER = 30;
    /**
     * 已结算
     */
    public static final int FINISH_ORDER = 35;
    /**
     * 已评价
     */
    public static final int RATED_ORDER = 40;
    /**
     * 已销单
     */
    public static final int CANCEL_ORDER = 45;

    public static String status2Str(int status) {
        switch (status) {
            case NEW_ORDER:
                return XApp.getInstance().getString(R.string.gw_new_order);
            case PAIDAN_ORDER:
                return XApp.getInstance().getString(R.string.gw_sended_order);
            case TAKE_ORDER:
                return XApp.getInstance().getString(R.string.gw_accepted_order);
            case GOTO_BOOKPALCE_ORDER:
                return XApp.getInstance().getString(R.string.gw_to_start);
            case ARRIVAL_BOOKPLACE_ORDER:
                return XApp.getInstance().getString(R.string.gw_arrive_start);
            case GOTO_DESTINATION_ORDER:
                return XApp.getInstance().getString(R.string.gw_to_end);
            case START_WAIT_ORDER:
                return XApp.getInstance().getString(R.string.gw_middle_wait);
            case ARRIVAL_DESTINATION_ORDER:
                return XApp.getInstance().getString(R.string.gw_arrive_end);
            case FINISH_ORDER:
                return XApp.getInstance().getString(R.string.gw_settled);
            case RATED_ORDER:
                return XApp.getInstance().getString(R.string.gw_exculated);
            case CANCEL_ORDER:
                return XApp.getInstance().getString(R.string.gw_canceled);
            default:
                return "";
        }
    }
}
