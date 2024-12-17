package com.shuke.alipaydemo.constant;

import com.alipay.api.AlipayClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 支付宝支付状态常量类
 */
public class AliPayConstant{
    // 支付成功
    public static final String TRADE_STATE_SUCCESS="TRADE_SUCCESS";

    // 退款成功
    public static final String REFUND_STATE_SUCCESS="REFUND_SUCCESS";
}