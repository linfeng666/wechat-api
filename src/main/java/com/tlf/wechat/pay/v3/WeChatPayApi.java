package com.tlf.wechat.pay.v3;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class WeChatPayApi {
    private static final Logger log = LoggerFactory.getLogger(WeChatPayApi.class);


    /**
     * 下单
     *
     * @param orderNumber
     * @param money
     * @param goodInfo
     * @param timeExpire
     * @param apiType     下单api类型：jsapi, nvtive, app, h5
     * @param openId
     * @param ip
     * @param wxConfig
     * @return
     */
    public JSONObject createOrder(
        String orderNumber,
        BigDecimal money,
        String goodInfo,
        String timeExpire,
        String apiType,
        String openId,
        String ip,
        WeChatPayConfig wxConfig
    ) {
        money = money.setScale(2, BigDecimal.ROUND_UP);

        String url = "";
        SortedMap<Object, Object> parameters = new TreeMap<>();
        switch (apiType) {
            case "jsapi":
                // JSAPI支付或小程序支付，请求方式POST，返回值：prepay_id（预支付交易会话标识）
                url = wxConfig.getJsApiUrl();
                parameters = WeChatPayService.parametersJSAPI(orderNumber, goodInfo, money, openId, wxConfig);
                break;
            case "native":
                // 扫码支付，请求方式POST，返回值：code_url（二维码链接）
                url = wxConfig.getNativeUrl();
                parameters = WeChatPayService.parametersNATIVE(orderNumber, goodInfo, money, wxConfig);
                break;
            case "h5":
                // H5 支付，请求方式POST，返回值：h5_url（支付跳转链接）
                url = wxConfig.getH5Url();
                parameters = WeChatPayService.parametersH5(orderNumber, goodInfo, money, ip, wxConfig);
                break;
            case "app":
                // APP 支付，请求方式POST，返回值：prepay_id（预支付交易会话标识）
                url = wxConfig.getH5Url();
                parameters = WeChatPayService.parametersApp(orderNumber, goodInfo, money, wxConfig);
                break;
        }

        // 订单过期时间
        if (ObjectUtils.isNotEmpty(timeExpire)) {
            parameters.put("time_expire", timeExpire);
        }

        // 2、转换成json字符串，开始微信支付请求
        String body = new GsonBuilder().create().toJson(parameters);

        // 3、扫码支付下单
        return WeChatPayHttpUtil.doPost(
            wxConfig.getMchId(),
            wxConfig.getMchSerialNo(),
            wxConfig.getV3Key(),
            wxConfig.getPrivateKey(),
            url,
            body
        );

    }


    /**
     * 关闭订单
     * 1、商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
     * 2、系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
     *
     * @param outTradeNo
     * @param wxConfig
     * @return
     */
    public boolean closeOrder(
        String outTradeNo,
        WeChatPayConfig wxConfig
    ) {
        try {
            // 请求URL
            String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/" + outTradeNo + "/close";

            SortedMap<Object, Object> parameters = new TreeMap<>();
            parameters.put("mchid", wxConfig.getMchId());
            // 将请求参数转换为json格式
            String data = new GsonBuilder().create().toJson(parameters);

            // 订单关闭
            JSONObject jsonObject = WeChatPayHttpUtil.doPost(
                wxConfig.getMchId(),
                wxConfig.getMchSerialNo(),
                wxConfig.getPrivateKey(),
                url,
                data
            );

            if (ObjectUtils.isEmpty(jsonObject)) {
                log.error("订单关闭失败：return body = null");
                return false;
            }

            Integer statusCode = jsonObject.getInteger("statusCode");
            if (statusCode == 200 || statusCode == 204) { //处理成功
                log.error("订单关闭成功：success, resp code = " + statusCode + ", return body = " + jsonObject.toJSONString());
                return true;
            }
            log.error("订单关闭失败：failed,resp code = " + statusCode + ", return body = " + jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("关闭订单异常：{}", e.getMessage());
        }
        return false;
    }


    /**
     * 查询订单
     * 是否支付成功，用于订单超时检测，返回SUCCESS则支付成功
     *
     * @param outTradeNo 订单号
     *                   SUCCESS：支付成功
     *                   REFUND：转入退款
     *                   NOTPAY：未支付
     *                   CLOSED：已关闭
     *                   REVOKED：已撤销（仅付款码支付会返回）
     *                   USERPAYING：用户支付中（仅付款码支付会返回）
     *                   PAYERROR：支付失败（仅付款码支付会返回）
     * @return 返回交易状态
     */
    public JSONObject selectOrderOutTradeNo(
        String outTradeNo,
        WeChatPayConfig wxConfig
    ) {
        String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/" +
            outTradeNo +
            "?mchid=" +
            wxConfig.getMchId();

        return WeChatPayHttpUtil.doGet(
            wxConfig.getMchId(),
            wxConfig.getMchSerialNo(),
            wxConfig.getPrivateKey(),
            url
        );
    }


    /**
     * 查询订单
     * 是否支付成功，用于订单超时检测，返回SUCCESS则支付成功
     *
     * @param transactionId 订单号
     *                      SUCCESS：支付成功
     *                      REFUND：转入退款
     *                      NOTPAY：未支付
     *                      CLOSED：已关闭
     *                      REVOKED：已撤销（仅付款码支付会返回）
     *                      USERPAYING：用户支付中（仅付款码支付会返回）
     *                      PAYERROR：支付失败（仅付款码支付会返回）
     * @return 返回交易状态
     */
    public JSONObject selectOrderByTransactionId(
        String transactionId,
        WeChatPayConfig wxConfig
    ) {
        String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/id/" +
            transactionId +
            "?mchid=" + wxConfig.getMchId();

        return WeChatPayHttpUtil.doGet(
            wxConfig.getMchId(),
            wxConfig.getMchSerialNo(),
            wxConfig.getPrivateKey(),
            url
        );
    }


    /**
     * 退款
     * 当交易发生之后一年内，由于买家或者卖家的原因需要退款时，卖家可以通过退款接口将支付金额退还给买家，
     * 微信支付将在收到退款请求并且验证成功之后，将支付款按原路退还至买家账号上。
     *
     * @param transactionId：微信支付订单号
     * @param out_trade_no：商户订单号
     * @param out_refund_no：商户退款单号
     * @param reason
     * @param refund
     * @param total
     * @param wxConfig
     * @return
     */
    public JSONObject refund(
        String transactionId,
        String out_trade_no,
        String out_refund_no,
        String reason,
        BigDecimal refund,
        BigDecimal total,
        WeChatPayConfig wxConfig
    ) {
        Map<Object, Object> parameters = new HashMap<>();
        parameters.put("transaction_id", transactionId);
        parameters.put("out_trade_no", out_trade_no);
        parameters.put("out_refund_no", out_refund_no);
        parameters.put("reason", reason);
        parameters.put("notify_url", wxConfig.getRefundNotifyUrl());

        Map<Object, Object> amount = new HashMap<>();
        amount.put("refund", WeChatPayUtil.getMoney(refund));
        amount.put("total", WeChatPayUtil.getMoney(total));
        amount.put("currency", "CNY");

        parameters.put("amount", amount);

        String data = new GsonBuilder().create().toJson(parameters);

        log.info("========== \n");
        log.info("refund data = " + data + "\n");
        log.info("========== \n");
        return WeChatPayHttpUtil.doPost(
            wxConfig.getMchId(),
            wxConfig.getMchSerialNo(),
            wxConfig.getPrivateKey(),
            wxConfig.getRefundUrl(),
            data
        );

    }


    /**
     * 查询退款订单
     * 是否支付成功，用于订单超时检测，返回SUCCESS则支付成功
     *
     * @param outTradeNo 订单号
     *                   SUCCESS：退款成功
     *                   CLOSED：退款关闭
     *                   PROCESSING：退款处理中
     *                   ABNORMAL：退款异常
     * @return 返回交易状态
     */
    public JSONObject selectRefundOrderOutTradeNo(
        String outTradeNo,
        WeChatPayConfig wxConfig
    ) {
        String url = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/" + outTradeNo;

        return WeChatPayHttpUtil.doGet(
            wxConfig.getMchId(),
            wxConfig.getMchSerialNo(),
            wxConfig.getPrivateKey(),
            url
        );
    }


    /**
     * 回调
     *
     * @param serialNumber
     * @param nonce
     * @param timestamp
     * @param signature
     * @param body
     * @param wxConfig
     * @return
     */
    public Map<String, Object> callBack(
        String serialNumber,
        String nonce,
        String timestamp,
        String signature,
        String body,
        WeChatPayConfig wxConfig
    ) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 回调验签和解密
            NotificationRequest Nrequest = new NotificationRequest.Builder().withSerialNumber(serialNumber)
                .withNonce(nonce)
                .withTimestamp(timestamp)
                .withSignature(signature)
                .withBody(body)
                .build();

            NotificationHandler handler = new NotificationHandler(
                WeChatPayUtil.getVerifier(
                    wxConfig.getMchId(),
                    wxConfig.getMchSerialNo(),
                    wxConfig.getV3Key(),
                    wxConfig.getPrivateKey()
                ),
                wxConfig.getV3Key().getBytes(StandardCharsets.UTF_8)
            );
            // 验签和解析请求体
            Notification notification = handler.parse(Nrequest);

            // 从notification中获取解密报文。
            String plainText = notification.getDecryptData();
            log.info("========== \n");
            log.info("plainText = " + plainText);
            log.info("========== \n");
            if (ObjectUtils.isNotEmpty(plainText)) {
                JSONObject resource = JSONObject.parseObject(plainText);
                // 成功应答
                map.put("code", "SUCCESS");
                map.put("message", "成功");
                map.put("status", 200);
                map.put("resource", resource);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("验签失败");
        }
        // 应答失败
        map.put("code", "ERROR");
        map.put("message", "验签失败");
        map.put("status", 500);
        return map;
    }


}
