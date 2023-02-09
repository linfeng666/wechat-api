package com.tlf.wechat.http;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static Integer connectTimeOutNumber = 10000;
    private static Integer socketTimeOutNumber = 30000;


    public static String getBody(
            String host,
            String path,
            String method,
            Map<String, String> headers,
            Map<String, String> querys,
            JSONObject formData,
            Integer timeOutNumber
    ) throws Exception {
        return getBody(host, path, method, headers, querys, formData.toJSONString(), timeOutNumber);
    }


    public static String getBody(
            String host,
            String path,
            String method,
            Map<String, String> headers,
            Map<String, String> querys,
            String formData,
            Integer timeOutNumber
    ) throws Exception {
        HttpResponse httpResponse = null;
        switch (method.toLowerCase()) {
            case "post":
                httpResponse = doPost(host, path, headers, querys, formData, timeOutNumber);
                break;
            case "get":
                httpResponse = doGet(host, path, headers, querys, timeOutNumber);
                break;
            case "put":
                httpResponse = doPut(host, path, headers, querys, formData, timeOutNumber);
                break;
            case "del":
            case "delete":
                httpResponse = doDelete(host, path, headers, querys, formData, timeOutNumber);
                break;
        }
        if (ObjectUtils.isEmpty(httpResponse)) {
            return null;
        }
        HttpEntity httpEntity = httpResponse.getEntity();
        int statusCode = httpResponse.getStatusLine().getStatusCode(); // 响应状态码
        String body = EntityUtils.toString(httpEntity); // 响应体
        if (statusCode == 200) {
            return body;
        }
        return null;
    }


    /**
     * get
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @return
     * @throws Exception
     */
    private static HttpResponse doGet(
            String host, String path,
            Map<String, String> headers,
            Map<String, String> querys,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpGet request = new HttpGet(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    /**
     * post form
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @param bodys
     * @return
     * @throws Exception
     */
    private static HttpResponse doPost(
            String host, String path,
            Map<String, String> headers,
            Map<String, String> querys,
            Map<String, String> bodys,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);
        HttpPost request = new HttpPost(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (bodys != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            for (String key : bodys.keySet()) {
                nameValuePairList.add(new BasicNameValuePair(key, bodys.get(key)));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, "utf-8");
            formEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
            request.setEntity(formEntity);
        }

        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    /**
     * Post String
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @param body
     * @return
     * @throws Exception
     */
    private static HttpResponse doPost(
            String host, String path,
            Map<String, String> headers,
            Map<String, String> querys,
            String body,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpPost request = new HttpPost(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, "utf-8"));
        }
        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    /**
     * Post stream
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @param body
     * @return
     * @throws Exception
     */
    private static HttpResponse doPost(
            String host, String path,
            Map<String, String> headers,
            Map<String, String> querys,
            byte[] body,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);
        HttpPost request = new HttpPost(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (body != null) {
            request.setEntity(new ByteArrayEntity(body));
        }

        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    /**
     * Put String
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @param body
     * @return
     * @throws Exception
     */
    private static HttpResponse doPut(
            String host,
            String path,
            Map<String, String> headers,
            Map<String, String> querys,
            String body,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpPut request = new HttpPut(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, "utf-8"));
        }

        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    /**
     * Put stream
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @param body
     * @return
     * @throws Exception
     */
    private static HttpResponse doPut(
            String host,
            String path,
            Map<String, String> headers,
            Map<String, String> querys,
            byte[] body,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);
        HttpPut request = new HttpPut(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (body != null) {
            request.setEntity(new ByteArrayEntity(body));
        }

        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    /**
     * Delete
     *
     * @param host
     * @param path
     * @param headers
     * @param querys
     * @return
     * @throws Exception
     */
    private static HttpResponse doDelete(
            String host,
            String path,
            Map<String, String> headers,
            Map<String, String> querys,
            String body,
            Integer timeOutNumber
    ) throws Exception {
        HttpClient httpClient = wrapClient(host);

        MyHttpDelete request = new MyHttpDelete(buildUrl(host, path, querys));
        if (ObjectUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                request.addHeader(e.getKey(), e.getValue());
            }
        }
        if (ObjectUtils.isNotEmpty(body)) {
            StringEntity entity = new StringEntity(body, "utf-8");
            request.setEntity(entity);
        }

        setTimeOut(request, timeOutNumber);
        return httpClient.execute(request);
    }


    private static void setTimeOut(HttpPost request, Integer timeOutNumber) {
        if (ObjectUtils.isEmpty(timeOutNumber)) {
            timeOutNumber = socketTimeOutNumber;
        }
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeOutNumber)    // 设置连接超时时间
                .setSocketTimeout(timeOutNumber).build();   // 设置响应超时时间
        // 将配置设置到里面
        request.setConfig(config);
    }


    private static void setTimeOut(MyHttpDelete request, Integer timeOutNumber) {
        if (ObjectUtils.isEmpty(timeOutNumber)) {
            timeOutNumber = socketTimeOutNumber;
        }
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeOutNumber)    // 设置连接超时时间
                .setSocketTimeout(timeOutNumber).build();   // 设置响应超时时间
        // 将配置设置到里面
        request.setConfig(config);
    }


    private static void setTimeOut(HttpPut request, Integer timeOutNumber) {
        if (ObjectUtils.isEmpty(timeOutNumber)) {
            timeOutNumber = socketTimeOutNumber;
        }
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeOutNumber)    // 设置连接超时时间
                .setSocketTimeout(timeOutNumber).build();   // 设置响应超时时间
        // 将配置设置到里面
        request.setConfig(config);
    }


    private static void setTimeOut(HttpGet request, Integer timeOutNumber) {
        if (ObjectUtils.isEmpty(timeOutNumber)) {
            timeOutNumber = socketTimeOutNumber;
        }
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeOutNumber)    // 设置连接超时时间
                .setSocketTimeout(timeOutNumber).build();   // 设置响应超时时间
        // 将配置设置到里面
        request.setConfig(config);
    }


    private static String buildUrl(String host, String path, Map<String, String> querys) throws UnsupportedEncodingException {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);
        if (!StringUtils.isBlank(path)) {
            sbUrl.append(path);
        }
        if (null != querys) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : querys.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append("&");
                }
                if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (!StringUtils.isBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StringUtils.isBlank(query.getValue())) {
                        sbQuery.append("=");
                        sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append("?").append(sbQuery);
            }
        }

        return sbUrl.toString();
    }


    private static HttpClient wrapClient(String host) {
        HttpClient httpClient = new DefaultHttpClient();
        if (host.startsWith("https://")) {
            sslClient(httpClient);
        }

        return httpClient;
    }


    private static void sslClient(HttpClient httpClient) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] xcs, String str) {

                }

                public void checkServerTrusted(X509Certificate[] xcs, String str) {

                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry registry = ccm.getSchemeRegistry();
            registry.register(new Scheme("https", 443, ssf));
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}