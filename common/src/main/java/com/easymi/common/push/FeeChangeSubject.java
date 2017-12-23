package com.easymi.common.push;

/**
 * Created by liuzihao on 2017/12/13.
 *
 * 位置变化的主题
 *
 */

public interface FeeChangeSubject {
    //添加观察者
    void addObserver(FeeChangeObserver obj);
    //移除观察者
    void deleteObserver(FeeChangeObserver obj);
    //当主题方法改变时,这个方法被调用,通知所有的观察者
    void notifyObserver(long orderId,String orderType);
}