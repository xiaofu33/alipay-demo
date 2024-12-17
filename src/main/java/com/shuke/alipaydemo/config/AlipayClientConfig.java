package com.shuke.alipaydemo.config;

import com.alipay.api.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝客户端配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayClientConfig {

    // 支付宝网关
    private String gateway;

    // 应用id
    private String appid;

    // 商户id
    private String pid;

    // 商户私钥
    private String privateKey;

    // 支付宝公钥
    private String publicKey;

    // 同步通知地址
    private String returnUrl;

    // 异步通知地址
    private String notifyUrl;

    /**
     * 获取支付宝客户端
     * @return
     * @throws AlipayApiException
     */
    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {
        AlipayConfig alipayConfig=new AlipayConfig();
        //设置appId
        alipayConfig.setAppId(this.getAppid());
        //设置商户私钥
        alipayConfig.setPrivateKey(this.getPrivateKey());
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(this.getPublicKey());
        //设置支付宝网关
        alipayConfig.setServerUrl(this.getGateway());
        //设置请求格式,固定值json.
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        //设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        //设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);

        // 获取client
        return new DefaultAlipayClient(alipayConfig);
    }

}