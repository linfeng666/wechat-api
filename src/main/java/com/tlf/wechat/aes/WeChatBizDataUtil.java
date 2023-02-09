package com.tlf.wechat.aes;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;


public class WeChatBizDataUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatBizDataUtil.class);

    /**
     * 根据微信数据和sessionkey生成用于校验的签名
     * <p>
     * rawData     微信数据
     * sessionKey  sessionkey
     */
    public static String getSignature2(
        String rawData,
        String sessionKey
    ) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String stringASCII = rawData + sessionKey;
        String signature2 = null;
        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(stringASCII.getBytes("UTF-8"));
            //获取字节数组
            byte messageDigest[] = digest.digest();
            // 创建 Hex 字符串
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
                signature2 = hexString.toString().toLowerCase();
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw e;
        }
        return signature2;
    }


    /**
     * 解密微信加密数据，对称解密使用的算法为 AES-128-CBC，数据采用PKCS#7填充。
     *
     * @param encryptedData 加密串
     * @param sessionKey    会话密钥
     * @param iv            解密算法初始向量
     * @return 解密后的数据
     */
    public static String decryptWxData(String encryptedData, String sessionKey, String iv) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            SecretKeySpec spec = new SecretKeySpec(Base64.decodeBase64(sessionKey), "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(Base64.decodeBase64(iv)));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(Base64.decodeBase64(encryptedData));
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, StandardCharsets.UTF_8);
                log.info(">>>>> 微信加密数据解析结果:{}", result);
                return result;
            }
        } catch (Exception e) {
            log.error(">>>>> 微信加密数据解析失败:", e);
        }
        return null;
    }


    /**
     * 解密微信加密数据，对称解密使用的算法为 AES-128-CBC，数据采用PKCS#7填充。
     *
     * @param encryptedData 加密串
     * @param sessionKey    会话密钥
     * @param iv            解密算法初始向量
     * @return 解密后的数据
     */
    public static String decryptWxDataOfHutool(String encryptedData, String sessionKey, String iv) {
        AES aes = new AES(
            Mode.CBC,
            Padding.NoPadding,
            Base64.decodeBase64(sessionKey),
            Base64.decodeBase64(iv)
        );
        byte[] resultByte = aes.decrypt(Base64.decodeBase64(encryptedData));
        if (null != resultByte && resultByte.length > 0) {
            // 删除解密后明文的补位字符
            int padNum = resultByte[resultByte.length - 1];
            if (padNum < 1 || padNum > 32) {
                padNum = 0;
            }
            resultByte = Arrays.copyOfRange(resultByte, 0, resultByte.length - padNum);
            String result = new String(resultByte, StandardCharsets.UTF_8);
            log.info(">>>>> 微信加密数据解析结果:{}", result);
            return result;
        }
        return null;
    }


//    public static void main(String[] args) {
//        String encryptedData = "MO31k2RfosLiZgJjykSkmd5X0b6Z5pkQ2evuhnf9ezclMF8E4kT1ORdTYdZDJ925IJvVA6s05E7EC9Hu21XG7gbJGUhHV1RZpCjteblb7hLTs+f5BDB1YmrgGLSv+ENKM2cYepN/8LWtsBsdZzF9jPLGn2mVg+CfKTCYm/HHr2riq1jzNSnETgVO+6H+a5R1AOgd33d0vh7EEBNbmvABw9navYfOQZEmMyXLoF4Pn2UTkwOqgfNyaJOcvOhbhqHRULrZ9N1c/BPub7gBS3hdbYbKsqf8GUHEl5SJXmg9WtsyZXO2gx+J2pn284RQ7LaW0kNGLzAzFUS85VDX87HZjF15OKD9Pn6UyigdlnxkIRBJ+OCQf8QPuEzTWDiMoadmK8HvE5j2B6X4Q7H6dKxT3T/Myg0vnFwC66uor/BtlYYqt/f8ZN/Qn7Q30wXK73Ih";
//        String iv = "1QWkCRCywXNz0WbsuncbOQ==";
//        String sessionKey = "ZSxvglD7D78+ufzuBLFzPQ==";
//
////        String encryptedData = "hcu6gpwxhO88gon85L4yt166leKyXZQuTv4qLNGSjuJDWmIETwpS18nbss6d/51hCLiI0puSddYRXgKXAOHOusjnkWDnhu6VLDNMo6fO3Ne92OlIB6ZmkS4JbedRqb69sLztJXkJ+2pabHuFmL6ptW1oqml5MEsQkFI/DhUpuKMwIHsBZOUBYVqoAEn3vl5pNwvpEOOU0gNzlv6OirMYyA==";
////        String sessionKey = "rtaiYpAF2aWlYNlTlhu3WQ==";
////        String iv = "AJwTuihrh5c0pP2aqY5GRw==";
//
//        String s = decryptWxDataOfHutool(encryptedData, sessionKey, iv);
//        System.out.println(s);
//
//        String s1 = decryptWxData(encryptedData, sessionKey, iv);
//        System.out.println(s1);
//    }

}