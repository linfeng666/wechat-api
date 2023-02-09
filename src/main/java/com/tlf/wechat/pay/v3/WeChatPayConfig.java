package com.tlf.wechat.pay.v3;


import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeChatPayConfig {
    /**
     * 平台证书列表地址
     */
    private String certificatesUrl = "https://api.mch.weixin.qq.com/v3/certificates";

    /**
     * native下单URL
     */
    private String nativeUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";

    /**
     * App下单URL
     */
    private String AppUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/app";

    /**
     * 小程序/jsapi 下单URL
     */
    private String jsApiUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

    /**
     * h5下单url
     */
    private String h5Url = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5";

    /**
     * 退款地址
     */
    private String refundUrl = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    /**
     * 转账地址
     */
    private String transfersUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";

    /**
     * APP调起支付订单详情扩展字符串
     */
    private String PACKAGE = "Sign=WXPay";

    /**
     * 调起支付签名方式
     */
    private String SignType = "RSA";

    /**
     * 定义全局容器 保存微信平台证书公钥
     */
    private Map<String, X509Certificate> certificateMap = new ConcurrentHashMap<>();

    /**
     * 微信证书私钥
     */
    private PrivateKey privateKey;

    /**
     * 微信开放平台的 APPID
     */
    private String appId;

    /**
     * 微信商户号
     */
    private String mchId;

    /**
     * 微信商家api序列号
     */
    private String mchSerialNo;

    /**
     * 回调报文解密V3密钥key
     */
    private String v3Key;

    /**
     * 异步接收微信支付结果通知的回调地址
     */
    private String notifyUrl;

    private String refundNotifyUrl;


    public WeChatPayConfig(
            String appId,
            String mchId,
            String mchSerialNo,
            String v3Key,
            String notifyUrl,
            String privateKeyPath,
            String refundNotifyUrl
    ) {
        this.appId = appId;
        this.mchId = mchId;
        this.mchSerialNo = mchSerialNo;
        this.v3Key = v3Key;
        this.notifyUrl = notifyUrl;
        this.refundNotifyUrl = refundNotifyUrl;
        try {
            this.privateKey = WeChatPayUtil.getPrivateKey(privateKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getCertificatesUrl() {
        return certificatesUrl;
    }

    public void setCertificatesUrl(String certificatesUrl) {
        this.certificatesUrl = certificatesUrl;
    }

    public String getNativeUrl() {
        return nativeUrl;
    }

    public void setNativeUrl(String nativeUrl) {
        this.nativeUrl = nativeUrl;
    }

    public String getAppUrl() {
        return AppUrl;
    }

    public void setAppUrl(String appUrl) {
        AppUrl = appUrl;
    }

    public String getJsApiUrl() {
        return jsApiUrl;
    }

    public void setJsApiUrl(String jsApiUrl) {
        this.jsApiUrl = jsApiUrl;
    }

    public String getPACKAGE() {
        return PACKAGE;
    }

    public void setPACKAGE(String PACKAGE) {
        this.PACKAGE = PACKAGE;
    }

    public String getSignType() {
        return SignType;
    }

    public void setSignType(String signType) {
        SignType = signType;
    }

    public Map<String, X509Certificate> getCertificateMap() {
        return certificateMap;
    }

    public void setCertificateMap(Map<String, X509Certificate> certificateMap) {
        this.certificateMap = certificateMap;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getMchSerialNo() {
        return mchSerialNo;
    }

    public void setMchSerialNo(String mchSerialNo) {
        this.mchSerialNo = mchSerialNo;
    }

    public String getV3Key() {
        return v3Key;
    }

    public void setV3Key(String v3Key) {
        this.v3Key = v3Key;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getH5Url() {
        return h5Url;
    }

    public void setH5Url(String h5Url) {
        this.h5Url = h5Url;
    }


    public String getRefundUrl() {
        return refundUrl;
    }

    public void setRefundUrl(String refundUrl) {
        this.refundUrl = refundUrl;
    }

    public String getRefundNotifyUrl() {
        return refundNotifyUrl;
    }

    public void setRefundNotifyUrl(String refundNotifyUrl) {
        this.refundNotifyUrl = refundNotifyUrl;
    }

    public String getTransfersUrl() {
        return transfersUrl;
    }

    public void setTransfersUrl(String transfersUrl) {
        this.transfersUrl = transfersUrl;
    }

}
