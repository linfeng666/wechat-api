package com.tlf.wechat.mini;

import com.alibaba.fastjson.JSONObject;
import com.tlf.wechat.WeChatHttpUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WeChatMiniUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatMiniUtil.class);


    /**
     * 获取 accessToken
     *
     * @return
     */
    public static String getAccessToken(String appId, String appSecret) {
        String access_token = null;
        try {
            String host = "https://api.weixin.qq.com/cgi-bin/token?";
            String path = "grant_type=client_credential" + "&appid=" + appId + "&secret=" + appSecret;
            log.info("url={}", host + path);

            JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "get", null, null, null);
            if (ObjectUtils.isEmpty(jsonObject)) {
                return null;
            }
            log.info("jsonObject={}", jsonObject);
            access_token = jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return access_token;
    }


    /**
     * 登陆
     *
     * @param appId
     * @param appSecret
     * @param code
     * @return
     */
    public static JSONObject code2Session(String appId, String appSecret, String code) {
        // 根据code请求wx接口拿到openid等信息
        String host = "https://api.weixin.qq.com/sns/jscode2session?";
        String path = "grant_type=authorization_code" + "&appid=" + appId + "&secret=" + appSecret + "&js_code=" + code;
        log.info("url={}", host + path);

        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "get", null, null, null);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        return jsonObject;
    }


    /**
     * 获取小程序链接
     */
    public static String getUrlLink(String appId, String appSecret, String miniPath, String query) throws Exception {
        String host = "https://api.weixin.qq.com/wxa/generate_urllink?";
        String path = "access_token=" + getAccessToken(appId, appSecret);

        Map<String, Object> formData = new HashMap<>();
        // 通过 URL Link 进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query 。path 为空时会跳转小程序主页
        // 页面路径
        formData.put("path", miniPath);
        if (ObjectUtils.isNotEmpty(query)) {
            formData.put("query", query);
        }
        // 默认值false。生成的 URL Link 类型，到期失效：true，永久有效：false
//        formData.put("is_expire", false);
        // 失效类型：默认值0，失效时间：0，失效间隔天数：1
//        formData.put("expire_type", 0);
        // 到期失效的 URL Link 的失效时间，为 Unix 时间戳。
//        Date date = new Date();
//        formData.put("expire_time", date.getTime());
        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "post", null, null, formData);
        log.info("====== \n");
        log.info(ObjectUtils.isNotEmpty(jsonObject) ? jsonObject.toJSONString() : "null");

        String errCode = jsonObject.getString("errcode");
        if (!errCode.equals("0")) {
            log.error("获取小程序链接失败");
            log.error(ObjectUtils.isNotEmpty(jsonObject) ? jsonObject.toJSONString() : "null");
            throw new Exception("获取小程序链接失败");
        }
        return jsonObject.getString("url_link");
    }


    /**
     * 获取手机号
     *
     * @param code
     * @return
     */
    public static String getPhone(String appId, String appSecret, String code) {
        String purePhoneNumber = null;
        try {
            String host = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?";
            String path = "access_token=" + getAccessToken(appId, appSecret);
            log.info("url={}", host + path);

            Map<String, Object> formData = new HashMap<>();
            formData.put("code", code);

            JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "post", null, null, formData);
            log.info("jsonObject={}", jsonObject);

            String errcode = jsonObject.getString("errcode");
            if (errcode.equals("0")) {
                JSONObject phone_info = jsonObject.getJSONObject("phone_info");
                purePhoneNumber = phone_info.getString("purePhoneNumber");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return purePhoneNumber;
    }


}
