package com.shuke.alipaydemo.model;

import lombok.Data;

@Data
public class RefundInfo {

    // 订单号
    private String orderNo;

    // 退款号
    private String refundId;

    // 退款金额
    private String totalFee;

    // 退款原因
    private String reason;
}
