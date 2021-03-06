package com.easymi.common.result;

import com.easymi.common.entity.MultipleOrder;
import com.easymi.component.result.EmResult;

import java.util.List;

/**
 * Copyright (C), 2012-2018, Sichuan Xiaoka Technology Co., Ltd.
 * FileName:
 * @Author: shine
 * Date: 2018/12/24 下午5:00
 * Description:
 * History:
 */

public class QueryOrdersResult extends EmResult {

    public List<MultipleOrder> orders;
    public int total;

    public List<MultipleOrder> data;
}
