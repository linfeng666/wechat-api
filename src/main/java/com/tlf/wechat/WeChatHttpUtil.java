package com.tlf.wechat;

import com.alibaba.fastjson.JSONObject;
import com.tlf.wechat.http.HttpUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * 微信http请求
 */
public class WeChatHttpUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatHttpUtil.class);

    /**
     * 发起请求
     *
     * @param host
     * @param path
     * @param method
     * @param formData
     * @return
     */
    public static JSONObject getBody(
            String host,
            String path,
            String method,
            Map<String, String> headers,
            Map<String, String> querys,
            Map<String, Object> formData
    ) {
        String body = null;
        if (ObjectUtils.isNotEmpty(formData)) {
            body = JSONObject.toJSONString(formData);
        }
        log.info("body: " + body + "\n");
        JSONObject jsonBody = null;
        try {
            String result = HttpUtil.getBody(host, path, method, headers, querys, body, 30);
            if (ObjectUtils.isNotEmpty(result)) {
                jsonBody = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("jsonBody: " + jsonBody + "\n");
        return jsonBody;
    }

}
