package com.tlf.wechat.oauth;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;


/**
 * 扫码登陆
 */
public class WeChatAuthUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatAuthUtil.class);

    private static JSONObject doGetJson(String url) {
        JSONObject jsonObject = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                jsonObject = JSONObject.parseObject(result);
            }
            httpGet.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    private static String generatorUrl(String appId, String scope, String domainUrl, String callBackPath) {
        try {
            String state = String.valueOf(new Date().getTime());
            String redirect_uri = URLEncoder.encode(domainUrl + callBackPath, "UTF-8");

            // appid:公众号的唯一标识
            // redirect_uri:授权后重定向的回调链接地址，请使用urlencode对链接进行处理
            // response_type:返回类型，请填写code
            // scope:应用授权作用域
            //      snsapi_base （不弹出授权页面，直接跳转，只能获取用户openid），
            //      snsapi_userinfo （弹出授权页面，可通过openid拿到昵称、性别、所在地。并且，即使在未关注的情况下，只要用户授权，也能获取其信息）
            // state:重定向后会带上state参数，开发者可以填写a-zA-Z0-9的参数值，最多128字节
            // wechat_redirect:无论直接打开还是做页面302重定向时候，必须带此参数
            return "https://open.weixin.qq.com/connect/qrconnect?" +
                    "appid=" + appId +
                    "&redirect_uri=" + redirect_uri +
                    "&response_type=code" +
                    "&scope=" + scope +
                    "&state=" + state +
                    "&lang=cn" +
                    "#wechat_redirect";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.warn("url编码错误");
        }
        return null;
    }


    /**
     * 网页扫码登陆二维码
     *
     * @return
     */
    public static String miniQrcUrl(String appId, String domainUrl, String callBackPath) {
        String scope = "snsapi_login";
        return generatorUrl(appId, scope, domainUrl, callBackPath);
    }


    /**
     * 公众号扫码登陆二维码
     *
     * @param callBackPath
     * @return
     */
    public static String pubQrcUrl(String appId, String domainUrl, String callBackPath) {
        String scope = "snsapi_userinfo";
        return generatorUrl(appId, scope, domainUrl, callBackPath);
    }


    /**
     * 刷新accessToken
     * @param appId
     * @param appsecret
     * @param code
     * @return
     */
    public static JSONObject accessToken(String appId, String appsecret, String code) {
        // 拼接url
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret="
                + appsecret + "&code=" + code + "&grant_type=authorization_code";
        return doGetJson(url);
    }


    /**
     * 微信用户信息
     * @param openid
     * @param access_token
     * @return
     */
    public static JSONObject userInfo(String openid, String access_token) {
        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + access_token +
                "&openid=" + openid +
                "&lang=zh_CN";
        return doGetJson(infoUrl);
    }

}
