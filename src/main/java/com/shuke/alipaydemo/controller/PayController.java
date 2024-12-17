package com.shuke.alipaydemo.controller;

import com.alipay.api.*;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.google.gson.Gson;
import com.shuke.alipaydemo.config.AlipayClientConfig;
import com.shuke.alipaydemo.model.AliPay;
import com.shuke.alipaydemo.model.RefundInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/alipay")
public class PayController {

    @Resource
    public AlipayClientConfig aliPayConfig;

    @Resource
    public AlipayClient alipayClient;

    /**
     * 支付宝创建支付接口
     * @return alipay 这里是需要传入一个请求类的，测试用就不写了
     * @throws Exception
     */
    @GetMapping("/pay")
    public String pay() throws Exception {
        // 创建电脑网站支付请求实例
        AlipayTradePagePayRequest request=new AlipayTradePagePayRequest();

        // 填入订单数据（测试用，随便填入）
        AlipayTradePagePayModel bizModel=new AlipayTradePagePayModel();
        bizModel.setOutTradeNo("1");
        bizModel.setTotalAmount("50.00");
        bizModel.setSubject("50元");
        bizModel.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 根据订单数据生成请求
        request.setBizModel(bizModel);
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());

        AlipayTradePagePayResponse response=null;
        try{
            // 调用 alipayClient 完成签名并执行请求
            response=alipayClient.pageExecute(request);
            if(response.isSuccess()){
                log.info("调用成功");
                return response.getBody();
            }
            else{
                log.error("调用失败");
                log.error(response.getMsg());
                return null;
            }
        }catch(AlipayApiException e){
            log.error("调用异常");
            return null;
        }

    }

    /**
     * 支付宝异步通知接口（支付宝验证支付成功后，会异步调用这个接口，来通知支付结果）
     * @param data
     * @return
     */
    @PostMapping("/notify")
    public String paySignal(@RequestBody Map<String,String> data){
        log.info("收到支付宝回调");
        //验签
        boolean signVerified=false;
        try{
            signVerified= AlipaySignature.rsaCheckV1(data,aliPayConfig.getPublicKey(),AlipayConstants.CHARSET_UTF8,AlipayConstants.SIGN_TYPE_RSA2);
            //验签成功
            if(signVerified){
                log.info("验签成功");
                // 业务逻辑：后端验证支付成功后所需要做的业务逻辑

                //除了success外其他返回均认为是失败
                return "success";
            }
            //验签失败
            else{
                log.error("验签失败");
                return "failure";
            }
        } catch(AlipayApiException e){
            log.error("验签异常");
            return "failure";
        }
    }

    /**
     * 订单查询(最主要用于查询订单的支付状态)
     * @param orderNo 订单号
     * @return
     */
    @GetMapping("/query")
    private Map<String,Object> queryPay(String orderNo){
        // 创建订单查询请求
        AlipayTradeQueryRequest request=new AlipayTradeQueryRequest();
        // 填入订单数据
        AlipayTradeQueryModel bizModel=new AlipayTradeQueryModel();
        bizModel.setOutTradeNo(orderNo);
        request.setBizModel(bizModel);
        try{
            //完成签名并执行请求
            AlipayTradeQueryResponse response=alipayClient.execute(request);
            if(response.isSuccess()){
                log.debug("查询订单{}成功",orderNo);
                Gson gson = new Gson();
                HashMap<String,Object> resultMap=gson.fromJson(response.getBody(),HashMap.class);
                return resultMap;
            }
            else{
                log.error("查询订单{}失败,响应数据是{}.",orderNo,response.getBody());
                return null;
            }
        }
        catch(AlipayApiException e){
            log.error("查询订单{}异常",orderNo);
            return null;
        }
    }

    /**
     * 取消支付
     * @param orderNo 订单号
     * @return
     */
    @PostMapping("/cancel")
    private boolean cancelPay(String orderNo){
        //请求
        AlipayTradeCloseRequest request=new AlipayTradeCloseRequest();
        //数据
        AlipayTradeCloseModel bizModel=new AlipayTradeCloseModel();
        bizModel.setOutTradeNo(orderNo);
        request.setBizModel(bizModel);
        try{
            //完成签名并执行请求
            AlipayTradeCloseResponse response=alipayClient.execute(request);
            if(response.isSuccess()){
                log.debug("订单{}取消成功",orderNo);
            }
            else{
                log.debug("订单{}未创建,因此也可认为本次取消成功.",orderNo);
            }
            return true;
        }
        catch(AlipayApiException e){
            log.error("订单{}取消异常",orderNo);
            return false;
        }
    }

    /**
     * 退款
     * @param refundInfo 需要封装一个退款请求类
     * @return
     */
    @PostMapping("/refund")
    private HashMap<String,Object> createRefund(RefundInfo refundInfo){
        //请求
        AlipayTradeRefundRequest request=new AlipayTradeRefundRequest();
        //数据
        AlipayTradeRefundModel bizModel=new AlipayTradeRefundModel();
        //订单号
        bizModel.setOutTradeNo(refundInfo.getOrderNo());
        //退款单号
        bizModel.setOutRequestNo(refundInfo.getRefundId());
        bizModel.setRefundAmount(refundInfo.getTotalFee().toString());
        bizModel.setRefundReason(refundInfo.getReason());
        request.setBizModel(bizModel);
        HashMap<String,Object> resultMap=new HashMap<>();
        try{
            //完成签名并执行请求
            AlipayTradeRefundResponse response=alipayClient.execute(request);
            //成功则说明退款成功了
            resultMap.put("data",response.getBody());
            if(response.isSuccess()){
                resultMap.put("isRefundSuccess",true);
                log.debug("订单{}退款成功",refundInfo.getOrderNo());
            }
            else{
                resultMap.put("isRefundSuccess",false);
                log.error("订单{}退款失败",refundInfo.getOrderNo());
            }
            return resultMap;
        }
        catch(AlipayApiException e){
            resultMap.put("isRefundSuccess",false);
            log.error("订单{}退款异常",refundInfo.getOrderNo());
            return resultMap;
        }
    }

    /**
     * 查询退款
     * @param refundInfo
     * @return
     */
    @GetMapping("/query-refund")
    private Map<String,Object> queryRefund(RefundInfo refundInfo){
        AlipayTradeFastpayRefundQueryRequest request=new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel bizModel=new AlipayTradeFastpayRefundQueryModel();
        //订单号
        bizModel.setOutTradeNo(refundInfo.getOrderNo());
        //退款单号
        bizModel.setOutRequestNo(refundInfo.getRefundId());
        //想要额外返回的数据(也就是文档中响应可选的数据)
        ArrayList<String> extraResponseDatas=new ArrayList<>();
        extraResponseDatas.add("refund_status");
        bizModel.setQueryOptions(extraResponseDatas);
        request.setBizModel(bizModel);
        try{
            //完成签名并执行请求
            AlipayTradeFastpayRefundQueryResponse response=alipayClient.execute(request);
            if(response.isSuccess()){
                log.debug("退款{}查询成功",refundInfo.getRefundId());
                Gson gson = new Gson();
                return gson.fromJson(response.getBody(),HashMap.class);
            }
            else{
                log.debug("退款{}查询失败",refundInfo.getRefundId());
                return null;
            }
        }
        catch(AlipayApiException e){
            log.debug("退款{}查询异常",refundInfo.getRefundId());
            return null;
        }
    }

    /**
     * 查询账单下载url
     * @param billType 账单类型,枚举值为1.trade(交易账单)2.signcustomer(流水账).
     * @param billDate 账单日期:
     *                 日账单:格式:yyyy-MM-dd 5.6日的账单记录的时间为05-06 9:00到05-07 9:00,并且在05-07 9:00后才能查到.
     *                 月账单:格式:yyyy-MM 8月的账单记录的时间为08-03到09-03,并且在09-03后才能查到.
     * @return 账单下载url(30s后则失效)
     */
    @GetMapping("/query-bill-download-url")
    public String queryBillDownloadUrl(String billType,String billDate){
        //请求
        AlipayDataDataserviceBillDownloadurlQueryRequest request=new AlipayDataDataserviceBillDownloadurlQueryRequest();
        //数据
        AlipayDataDataserviceBillDownloadurlQueryModel bizModel=new AlipayDataDataserviceBillDownloadurlQueryModel();
        bizModel.setBillType(billType);
        bizModel.setBillDate(billDate);
        request.setBizModel(bizModel);
        try{
            //完成签名并执行请求
            AlipayDataDataserviceBillDownloadurlQueryResponse response=alipayClient.execute(request);
            if(response.isSuccess()){
                log.debug("获取账单下载url成功");
                return response.getBillDownloadUrl();
            }
            else{
                log.error("获取账单下载url失败");
                return null;
            }
        }
        catch(AlipayApiException e){
            log.error("获取账单下载url异常");
            return null;
        }
    }
}
