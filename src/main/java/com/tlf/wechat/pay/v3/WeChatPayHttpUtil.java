package com.tlf.wechat.pay.v3;


import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.security.PrivateKey;


public class WeChatPayHttpUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatPayHttpUtil.class);


    /**
     * token方式
     *
     * @param mchId
     * @param mchSerialNo
     * @param privateKey
     * @param url
     * @return
     */
    public static JSONObject doGet(
            String mchId,
            String mchSerialNo,
            PrivateKey privateKey,
            String url
    ) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("Accept", "application/json");
        try {
            /**
             * 方法一：token方式
             */
            // 获取token
            String token = WeChatPayUtil.getToken(mchId, mchSerialNo, privateKey, "GET", new URL(url), "");
            // 设置Authorization
            httpGet.addHeader("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);

            // 发起请求
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();

            // 响应状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String jsonResult = EntityUtils.toString(httpEntity);
            log.info("========== \n");
            log.info("body = " + jsonResult);
            log.info("========== \n");
            if (statusCode == 200) {
                return JSONObject.parseObject(jsonResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 自动签名和验签
     *
     * @param mchId
     * @param mchSerialNo
     * @param privateKey
     * @param url
     * @return
     */
    public static JSONObject doGet(
            String mchId,
            String mchSerialNo,
            String v3Key,
            PrivateKey privateKey,
            String url
    ) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("Accept", "application/json");
        try {
            Verifier verifier = WeChatPayUtil.getVerifier(
                    mchId,
                    mchSerialNo,
                    v3Key,
                    privateKey
            );

            /**
             * 方式二：自动签名和验签
             */
            httpClient = WeChatPayUtil.getWxPayClient(verifier,
                    mchId,
                    mchSerialNo,
                    privateKey
            );

            // 发起请求
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();

            // 响应状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String jsonResult = EntityUtils.toString(httpEntity);
            log.info("========== \n");
            log.info("body = " + jsonResult);
            log.info("========== \n");
            if (statusCode == 200) {
                return JSONObject.parseObject(jsonResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * token
     *
     * @param mchId
     * @param mchSerialNo
     * @param privateKey
     * @param url
     * @param body
     * @return
     */
    public static JSONObject doPost(
            String mchId,
            String mchSerialNo,
            PrivateKey privateKey,
            String url,
            String body
    ) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;chartset=utf-8");
        httpPost.addHeader("Accept", "application/json");
        try {
            // 获取token
            String token = WeChatPayUtil.getToken(mchId, mchSerialNo, privateKey, "POST", new URL(url), body);
            // 设置Authorization
            httpPost.setHeader("Authorization", "WECHATPAY2-SHA256-RSA2048 " + token);

            // 构建消息实体
            StringEntity stringEntity = new StringEntity(body, "utf-8");
            httpPost.setEntity(stringEntity);

            // 发起请求
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // 响应状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String jsonResult = EntityUtils.toString(httpEntity);
            log.info("========== \n");
            log.info("body = " + jsonResult);
            log.info("========== \n");
            if (statusCode == 200) {
                return JSONObject.parseObject(jsonResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 自动签名和验签
     *
     * @param mchId
     * @param mchSerialNo
     * @param privateKey
     * @param url
     * @param body
     * @return
     */
    public static JSONObject doPost(
            String mchId,
            String mchSerialNo,
            String v3Key,
            PrivateKey privateKey,
            String url,
            String body
    ) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;chartset=utf-8");
        httpPost.addHeader("Accept", "application/json");
        try {
            Verifier verifier = WeChatPayUtil.getVerifier(
                    mchId,
                    mchSerialNo,
                    v3Key,
                    privateKey
            );

            /**
             * 方式二：自动签名和验签
             */
            httpClient = WeChatPayUtil.getWxPayClient(verifier,
                    mchId,
                    mchSerialNo,
                    privateKey
            );

            // 构建消息实体
            StringEntity stringEntity = new StringEntity(body, "utf-8");
            httpPost.setEntity(stringEntity);

            // 发起请求
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // 响应状态码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // 响应体
            String jsonResult = EntityUtils.toString(httpEntity);
            log.info("========== \n");
            log.info("body = " + jsonResult);
            log.info("========== \n");
            if (statusCode == 200) {
                return JSONObject.parseObject(jsonResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}