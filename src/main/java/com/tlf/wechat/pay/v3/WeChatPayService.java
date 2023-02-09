package com.tlf.wechat.pay.v3;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;


public class WeChatPayService {
    private static final Logger log = LoggerFactory.getLogger(WeChatPayService.class);


    /**
     * JSAPI 下单返回参数
     *
     * @param prepay_id
     * @param wxConfig
     * @return
     */
    public static Map<String, Object> JSAPIParameters(
            String prepay_id,
            WeChatPayConfig wxConfig
    ) {
        try {
            String noncestr = WeChatPayUtil.getNonceStr(32);
            String time = System.currentTimeMillis() / 1000 + "";

            ArrayList<String> list = new ArrayList<>();
            list.add(wxConfig.getAppId());
            list.add(time);
            list.add(noncestr);
            list.add("prepay_id=" + prepay_id);

            // 二次签名,调起支付需要重新签名, 注意这个是buildSignMessage()
            String paySign = WeChatPayUtil.sign(WeChatPayUtil.buildSignMessage(list).getBytes(), wxConfig.getPrivateKey());

            Map<String, Object> map = new HashMap<>();
            map.put("appId", wxConfig.getAppId());
            map.put("timeStamp", time);
            map.put("nonceStr", noncestr);
            map.put("package", "prepay_id=" + prepay_id);
            map.put("signType", wxConfig.getSignType());
            map.put("paySign", paySign);
            return map;
        } catch (Exception e) {
            log.error("组装微信支付JSAPI数据失败！预支付ID为：{}", prepay_id);
            e.printStackTrace();
        }
        return null;
    }


    /**
     * APP 下单返回参数
     * 组装支付数据给app，用于发起微信支付
     * 在支付过程中，有些已经有了预订单ID，需要再下单，直接传入预订单ID就可以发起支付了
     *
     * @param prepay_id
     * @param wxConfig
     * @return
     */
    public static Map<String, Object> AppParameters(
            String prepay_id,
            WeChatPayConfig wxConfig
    ) {
        try {
            String noncestr = WeChatPayUtil.getNonceStr(32);
            String time = System.currentTimeMillis() / 1000 + "";

            /**
             * 应用id
             * 时间戳
             * 随机字符串
             * 预支付交易会话ID
             */
            ArrayList<String> list = new ArrayList<>();
            list.add(wxConfig.getAppId());
            list.add(time);
            list.add(noncestr);
            list.add(prepay_id);

            // 二次签名,调起支付需要重新签名, 注意这个是buildSignMessage()
            String packageSign = WeChatPayUtil.sign(WeChatPayUtil.buildSignMessage(list).getBytes(), wxConfig.getPrivateKey());

            // 下单参数
            Map<String, Object> map = new HashMap<>();
            map.put("appid", wxConfig.getAppId());
            map.put("timestamp", time);
            map.put("noncestr", noncestr);
            map.put("prepayid", prepay_id);

            map.put("partnerid", wxConfig.getMchId());
            map.put("package", wxConfig.getPACKAGE());
            map.put("sign", packageSign);
            return map;
        } catch (Exception e) {
            log.error("组装微信支付APP数据失败！预支付ID为：{}", prepay_id);
            e.printStackTrace();
        }
        return null;
    }


    /**
     * APP下单
     *
     * @param orderNumber
     * @param goodInfo
     * @param money
     * @param wxConfig
     * @return
     */
    public static SortedMap<Object, Object> parametersApp(
            String orderNumber,
            String goodInfo,
            BigDecimal money,
            WeChatPayConfig wxConfig
    ) {
        SortedMap<Object, Object> parameters = new TreeMap<>();

        parameters.put("appid", wxConfig.getAppId());
        parameters.put("mchid", wxConfig.getMchId());
        parameters.put("description", goodInfo);
        parameters.put("out_trade_no", orderNumber);
        parameters.put("notify_url", wxConfig.getNotifyUrl());

        // 附加数据
        parameters.put("attach", orderNumber);

        HashMap<String, Object> amount = new HashMap<>();
        amount.put("total", WeChatPayUtil.getMoney(money));//需要将输入的金额*100变成分
        amount.put("currency", "CNY");
        parameters.put("amount", amount);

        return parameters;
    }


    /**
     * JSAPI下单（小程序下单）
     *
     * @param orderNumber
     * @param goodInfo
     * @param money
     * @param openId
     * @param wxConfig
     * @return
     */
    public static SortedMap<Object, Object> parametersJSAPI(
            String orderNumber,
            String goodInfo,
            BigDecimal money,
            String openId,
            WeChatPayConfig wxConfig
    ) {
        // 请求body参数
        SortedMap<Object, Object> parameters = new TreeMap<>();
        parameters.put("appid", wxConfig.getAppId());
        parameters.put("mchid", wxConfig.getMchId());
        parameters.put("out_trade_no", orderNumber);
        parameters.put("description", goodInfo);
        parameters.put("notify_url", wxConfig.getNotifyUrl());
        // 附加数据
        parameters.put("attach", orderNumber);

        HashMap<String, Object> amount = new HashMap<>();
        amount.put("total", WeChatPayUtil.getMoney(money));//需要将输入的金额*100变成分
        amount.put("currency", "CNY");
        parameters.put("amount", amount);

        HashMap<String, Object> payer = new HashMap<>();
        payer.put("openid", openId);
        parameters.put("payer", payer);

        return parameters;
    }


    /**
     * NATIVE下单
     *
     * @param orderNumber
     * @param goodInfo
     * @param money
     * @param wxConfig
     * @return
     */
    public static SortedMap<Object, Object> parametersNATIVE(
            String orderNumber,
            String goodInfo,
            BigDecimal money,
            WeChatPayConfig wxConfig
    ) {
        // 请求body参数
        SortedMap<Object, Object> parameters = new TreeMap<>();

        parameters.put("appid", wxConfig.getAppId());
        parameters.put("mchid", wxConfig.getMchId());
        parameters.put("out_trade_no", orderNumber);
        parameters.put("description", goodInfo);
        parameters.put("notify_url", wxConfig.getNotifyUrl());
        // 附加数据
        parameters.put("attach", orderNumber);

        HashMap<String, Object> amount = new HashMap<>();
        amount.put("total", WeChatPayUtil.getMoney(money));//需要将输入的金额*100变成分
        amount.put("currency", "CNY");
        parameters.put("amount", amount);
        return parameters;
    }


    /**
     * H5下单
     *
     * @param orderNumber
     * @param goodInfo
     * @param money
     * @param ip
     * @param wxConfig
     * @return
     */
    public static SortedMap<Object, Object> parametersH5(
            String orderNumber,
            String goodInfo,
            BigDecimal money,
            String ip,
            WeChatPayConfig wxConfig
    ) {
        // 请求body参数
        SortedMap<Object, Object> parameters = new TreeMap<>();

        parameters.put("appid", wxConfig.getAppId());
        parameters.put("mchid", wxConfig.getMchId());
        parameters.put("out_trade_no", orderNumber);
        parameters.put("description", goodInfo);
        parameters.put("notify_url", wxConfig.getNotifyUrl());
        // 附加数据
        parameters.put("attach", orderNumber);

        HashMap<String, Object> amount = new HashMap<>();
        //需要将输入的金额*100变成分
        amount.put("total", WeChatPayUtil.getMoney(money));
        amount.put("currency", "CNY");
        parameters.put("amount", amount);

        // 场景信息
        HashMap<String, Object> sceneInfo = new HashMap<>();
        sceneInfo.put("payer_client_ip", ip);
        HashMap<String, Object> H5Info = new HashMap<>();
        // 场景类型：示例值：iOS, Android, Wap
        H5Info.put("type", "iOS");
        sceneInfo.put("h5_info", H5Info);
        parameters.put("scene_info", sceneInfo);

        return parameters;
    }


}
