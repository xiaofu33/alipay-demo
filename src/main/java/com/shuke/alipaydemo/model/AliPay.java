package com.shuke.alipaydemo.model;

import lombok.Data;

/**
 * 支付请求类
 */
@Data
public class AliPay {
    // 商户订单号
    private String traceNo;

    // 支付金额
    private Double totalAmount;

    // 商品详情
    private String subject;

    // 支付宝交易号
    private String alipayTraceNo;
}
