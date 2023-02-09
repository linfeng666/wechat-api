package com.tlf.wechat.pay.v3;


import cn.hutool.core.io.resource.ClassPathResource;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.wechat.pay.contrib.apache.httpclient.constant.WechatPayHttpHeaders.*;


public class WeChatPayUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatPayUtil.class);

    public static Integer getMoney(BigDecimal money) {
        if (money == null) {
            return 0;
        }
        return money.multiply(new BigDecimal("100")).intValue();
    }

    /**
     * 元转换成分
     *
     * @param money
     * @return
     */
    public static Integer getMoney(String money) {
        if (money == null) {
            return 0;
        }
        // 金额转化为分为单位
        // 处理包含, ￥ 或者$的金额
        String currency = money.replaceAll("\\$|\\￥|\\,", "");
        int index = currency.indexOf(".");
        int length = currency.length();
        Long amLong = 0l;
        if (index == -1) {
            amLong = Long.valueOf(currency + "00");
        } else if (length - index >= 3) {
            amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
        } else if (length - index == 2) {
            amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
        } else {
            amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
        }
        return amLong.intValue();
    }


    /**
     * 生成随机数
     *
     * @return
     */
    public static String getNonceStr() {
        return UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, 32);
    }


    /**
     * 获取随机字符串
     *
     * @param length 字符串长度
     * @return 返回随机生成的字符串
     */
    public static String getNonceStr(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(str.length());
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


    /**
     * 加载本地证书
     * 证书私钥，32位设置的秘钥，从微信商户中设置后获得
     *
     * @return
     */
    private static String privateKey(String fileName) {
        InputStream inputStream = new ClassPathResource(
                fileName.replace("classpath:", "")
        ).getStream();

        String content = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining(System.lineSeparator()));

        return content.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

    }


    /**
     * 获取私钥。
     *
     * @return 私钥对象
     */
    public static PrivateKey getPrivateKey(String fileName) {
        // 加载商户私钥（privateKey：私钥字符串）
        return PemUtil.loadPrivateKey(
                new ByteArrayInputStream(
                        privateKey(fileName).getBytes(StandardCharsets.UTF_8)
                )
        );
    }


    /**
     * 生成 token 把请求地址URL和参数进行加密处理
     * 为什么要签名：对请求参数和请求地址进行加密
     *
     * @param mchId
     * @param mchSerialNo
     * @param privateKey
     * @param method
     * @param url
     * @param body
     * @return
     * @throws Exception
     */
    public static String getToken(
            String mchId,
            String mchSerialNo,
            PrivateKey privateKey,
            String method,
            URL url,
            String body
    ) throws Exception {
        // 生成随机数
        String nonceStr = getNonceStr();
        // 时间戳
        long timestamp = System.currentTimeMillis() / 1000;
        // 构成签名串
        String message = buildMessage(method, url, timestamp, nonceStr, body);
        // 签名
        String signature = sign(message.getBytes("utf-8"), privateKey);

        return "mchid=\"" + mchId + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + mchSerialNo + "\","
                + "signature=\"" + signature + "\"";
    }


    /**
     * 生成签名
     *
     * @param message
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String sign(
            byte[] message,
            PrivateKey privateKey
    ) throws Exception {
        Signature sign = Signature.getInstance("SHA256withRSA");
        // 商户私钥
        sign.initSign(privateKey);
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }


    /**
     * 签名校验
     * url https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/chapter5_1_13.shtml
     */
    public static Map<String, Object> verifySign(
            HttpServletRequest request,
            String mchId,
            String mchSerialNo,
            String v3Key,
            PrivateKey privateKey
    ) {
        Map<String, Object> signMap = new HashMap<>();
        signMap.put("flag", false);
        signMap.put("body", null);

        // 检查header
        String[] headers = {WECHAT_PAY_SERIAL, WECHAT_PAY_SIGNATURE, WECHAT_PAY_NONCE, WECHAT_PAY_TIMESTAMP};
        for (String headerName : headers) {
            if (request.getHeader(headerName) == null) {
                log.info("{} is null", headerName);
                return signMap;
            }
        }
        // 检查时间
        String timestamp = request.getHeader(WECHAT_PAY_TIMESTAMP);
        Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
        if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= 5) {
            log.info("超过应答时间");
            return signMap;
        }
        // 获取微信返回的参数
        String body;
        try {
            body = request.getReader().lines().collect(Collectors.joining());
        } catch (IOException e) {
            log.error("获取微信V3回调参数失败", e);
            return signMap;
        }
        log.info("-----------------------------------------");
        log.info("data = {}", body);
        log.info("-----------------------------------------");
        // 校验签名
        String nonce = request.getHeader(WECHAT_PAY_NONCE);
        String message = timestamp + "\n" + nonce + "\n" + body + "\n";
        String serial = request.getHeader(WECHAT_PAY_SERIAL);
        String signature = request.getHeader(WECHAT_PAY_SIGNATURE);

        try {
            Verifier verifier = WeChatPayUtil.getVerifier(
                    mchId,
                    mchSerialNo,
                    v3Key,
                    privateKey
            );
            if (!verifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature)) {
                log.info("签名校验失败");
                return signMap;
            }
            signMap.put("flag", true);
            signMap.put("body", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMap;
    }


    /**
     * 更新平台证书，可用定时器
     *
     * @param certificatesUrl
     * @param v3Key
     * @param certificateMap
     * @return
     * @throws Exception
     */
    public static Map<String, X509Certificate> refreshCertificate(
            String mchId,
            String mchSerialNo,
            PrivateKey privateKey,
            String certificatesUrl,
            String v3Key,
            Map<String, X509Certificate> certificateMap
    ) throws Exception {
        // 1、执行get请求
        JSONObject jsonObject = WeChatPayHttpUtil.doGet(mchId, mchSerialNo, privateKey, certificatesUrl);
        // 2、获取平台验证的相关参数信息
        if (ObjectUtils.isNotEmpty(jsonObject)) {
            JSONObject data = jsonObject.getJSONObject("data");
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    Object o = data.get(i);
                    JSONObject json = JSONObject.parseObject(o.toString());
                    JSONObject encrypt_certificate = json.getJSONObject("encrypt_certificate");
                    // 对关键信息进行解密
                    AesUtil aesUtil = new AesUtil(v3Key.getBytes());
                    String associated_data = encrypt_certificate.get("associated_data").toString().replaceAll("\"", "");
                    String nonce = encrypt_certificate.get("nonce").toString().replaceAll("\"", "");
                    String ciphertext = encrypt_certificate.get("ciphertext").toString().replaceAll("\"", "");
                    // 证书内容
                    String certStr = aesUtil.decryptToString(associated_data.getBytes(), nonce.getBytes(), ciphertext);
                    // 证书内容转成证书对象
                    CertificateFactory cf = CertificateFactory.getInstance("X509");
                    X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(
                            new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8))
                    );
                    String serial_no = json.get("serial_no").toString().replaceAll("\"", "");
                    certificateMap.put(serial_no, x509Cert);
                }
            }
        }
        return certificateMap;
    }


    /**
     * 下单 构造签名串
     *
     * @param method
     * @param url
     * @param timestamp
     * @param nonceStr
     * @param body
     * @return
     */
    public static String buildMessage(
            String method,
            URL url,
            long timestamp,
            String nonceStr,
            String body
    ) {
        // 第二步，获取请求的绝对URL，并去除域名部分得到参与签名的URL。
        // 如果请求中有查询参数，URL末尾应附加有'?'和对应的查询字符串。
        String canonicalUrl = url.getPath();
        if (url.getQuery() != null) {
            canonicalUrl += "?" + url.getQuery();
        }
        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }


    /**
     * APP调起支付 构造签名串
     *
     * @param signMessage 待签名的参数
     * @return 构造后带待签名串
     */
    public static String buildSignMessage(
            ArrayList<String> signMessage
    ) {
        if (signMessage == null || signMessage.size() <= 0) {
            return null;
        }
        StringBuilder sbf = new StringBuilder();
        for (String str : signMessage) {
            sbf.append(str).append("\n");
        }
        return sbf.toString();
    }


    /**
     * 获取请求体
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static String getRequestBody(
            HttpServletRequest request
    ) throws IOException {
        ServletInputStream stream = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            stream = request.getInputStream();
            // 获取响应
            reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new IOException("读取返回支付接口数据流出现异常！");
        } finally {
            reader.close();
        }
        return sb.toString();
    }


    /**
     * 处理返回的回调对象
     *
     * @param request
     * @return
     */
    public static String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null; ) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * 获取证书
     *
     * @return
     * @throws Exception
     */
    public static Verifier getVerifier(
            String mchId,
            String mchSerialNo,
            String v3Key,
            PrivateKey privateKey
    ) throws Exception {
        // 获取证书管理器实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();
        // 向证书管理器增加需要自动更新平台证书的商户信息
        certificatesManager.putMerchant(
                mchId,
                new WechatPay2Credentials(
                        mchId,
                        new PrivateKeySigner(mchSerialNo, privateKey)
                ),
                v3Key.getBytes(StandardCharsets.UTF_8)
        );
        // 从证书管理器中获取verifier
        return certificatesManager.getVerifier(mchId);
    }


    /**
     * 自动签名和验签
     *
     * @param verifier
     * @param mchId
     * @param mchSerialNo
     * @param privateKey
     * @return
     */
    public static CloseableHttpClient getWxPayClient(
            Verifier verifier,
            String mchId,
            String mchSerialNo,
            PrivateKey privateKey
    ) {
        //  把 httpClient.execute(httpPost) 换成 WeChatPayUtil.getWxPayClient(Verifier verifier).execute(httpPost);
        //  通过 WechatPayHttpClientBuilder 构造的 HttpClient，会自动的处理签名和验签，并进行证书自动更新
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create();
        builder.withMerchant(mchId, mchSerialNo, privateKey);
        builder.withValidator(new WechatPay2Validator(verifier));
        return builder.build();
    }


    /**
     * 微信回调解密
     *
     * @param associated_data 附加数据
     * @param nonce           数据密文
     * @param ciphertext      加密使用的随机串
     * @return 返回解密后的Map参数集合
     */
    public static Map callbackDecryption(String associated_data, String nonce, String ciphertext, String v3Key) {
        try {
            byte[] key = v3Key.getBytes(StandardCharsets.UTF_8);
            WeChatPayAesUtil weChatPayAesUtil = new WeChatPayAesUtil(key);
            String decryptToString = weChatPayAesUtil.decryptToString(
                    associated_data.getBytes(StandardCharsets.UTF_8),
                    nonce.getBytes(StandardCharsets.UTF_8),
                    ciphertext
            );
            return new Gson().fromJson(decryptToString, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}