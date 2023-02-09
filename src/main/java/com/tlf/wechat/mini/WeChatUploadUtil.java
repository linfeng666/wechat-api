package com.tlf.wechat.mini;

import com.alibaba.fastjson.JSONObject;
import com.tlf.wechat.WeChatHttpUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WeChatUploadUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatMiniUtil.class);


    /**
     * 上传临时素材
     *
     * @param appId
     * @param appSecret
     * @param fileName
     * @return
     */
    public static JSONObject upload(String appId, String appSecret, String fileName) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonObject = null;
        try {
            String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
            if (ObjectUtils.isEmpty(access_token)) {
                throw new Exception("appId 或 appSecret 错误");
            }
            String url = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
            url = url.replace("ACCESS_TOKEN", access_token).replace("TYPE", "image");

            HttpPost httpPost = new HttpPost(url);

            // 每个post参数之间的分隔。随意设定，只要不会和其他的字符串重复即可。
            String boundary = String.valueOf(System.currentTimeMillis());

            //设置请求头
            httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
            //HttpEntity builder
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //字符编码
            builder.setCharset(StandardCharsets.UTF_8);
            //模拟浏览器
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);

            //boundary
            builder.setBoundary(boundary);

            //multipart/form-data
            File file = new File(fileName);
            builder.addPart("media", new FileBody(file, ContentType.DEFAULT_BINARY));

            // binary
//            builder.addBinaryBody("name=\"file\"; filename=\"mysql.docx\"", new FileInputStream(file), ContentType.MULTIPART_FORM_DATA, fileName);// 文件流

            //其他参数
//            builder.addTextBody("file_name", fileName, ContentType.create("text/plain", Consts.UTF_8));
//            builder.addTextBody("file_name", fileName, ContentType.MULTIPART_FORM_DATA);
//            builder.addTextBody("file_code", "111111", ContentType.MULTIPART_FORM_DATA);
            //HttpEntity
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            // 执行提交
            HttpResponse response = httpClient.execute(httpPost);
            //响应
            HttpEntity responseEntity = response.getEntity();
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 && responseEntity != null) {
                // 将响应内容转换为字符串
                String result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }


    public static JSONObject download(String appId, String appSecret, String media_id) {
//        String url = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
        String host = "https://api.weixin.qq.com/cgi-bin/media/get?";
        String path = "access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
        log.info("url={}", host + path);

        String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
        if (ObjectUtils.isEmpty(access_token)) {
            return null;
        }
        path = path.replace("ACCESS_TOKEN", access_token);

        Map<String, String> querys = new HashMap<>();
        querys.put("media_id", media_id);

        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "get", null, querys, null);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        return jsonObject;
    }


    /**
     * 上传永久素材
     *
     * @param appId
     * @param appSecret
     * @param fileName
     * @return
     */
    public static JSONObject uploadLong(
        String appId,
        String appSecret,
        String fileName,
        String type,
        String title,
        String introduction
    ) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonObject = null;
        try {
            String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
            if (ObjectUtils.isEmpty(access_token)) {
                throw new Exception("appId 或 appSecret 错误");
            }
            String url = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN&type=TYPE";
            url = url.replace("ACCESS_TOKEN", access_token).replace("TYPE", type);

            HttpPost httpPost = new HttpPost(url);

            // 每个post参数之间的分隔。随意设定，只要不会和其他的字符串重复即可。
            String boundary = String.valueOf(System.currentTimeMillis());

            //设置请求头
            httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
            //HttpEntity builder
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //字符编码
            builder.setCharset(StandardCharsets.UTF_8);
            //模拟浏览器
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);

            //boundary
            builder.setBoundary(boundary);

            //multipart/form-data
            File file = new File(fileName);
            builder.addPart("media", new FileBody(file, ContentType.DEFAULT_BINARY));
            if (type.equals("video")) {
                if (ObjectUtils.isEmpty(title) || ObjectUtils.isEmpty(introduction)) {
                    throw new Exception("视频素材标题或素材描述不能为空");
                }
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("introduction", introduction);
                builder.addTextBody("description", JSONObject.toJSONString(params), ContentType.MULTIPART_FORM_DATA);
            }

            // binary
//            builder.addBinaryBody("name=\"file\"; filename=\"mysql.docx\"", new FileInputStream(file), ContentType.MULTIPART_FORM_DATA, fileName);// 文件流

            //其他参数
//            builder.addTextBody("file_name", fileName, ContentType.create("text/plain", Consts.UTF_8));
//            builder.addTextBody("file_name", fileName, ContentType.MULTIPART_FORM_DATA);
//            builder.addTextBody("file_code", "111111", ContentType.MULTIPART_FORM_DATA);
            //HttpEntity
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            // 执行提交
            HttpResponse response = httpClient.execute(httpPost);
            //响应
            HttpEntity responseEntity = response.getEntity();
            // 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 && responseEntity != null) {
                // 将响应内容转换为字符串
                String result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                jsonObject = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }


    //main 方法
    public static void main(String[] args) {
        String appId = "wx4eb5de5673e10a8a";
        String appSecret = "b6733e39c3ad465bd78158633a9811b6";

//        String appId = "wxdc155df46f50a1ff";
//        String appSecret = "bc5995eb532896c27a1a73f6d40b18da";


//        String fileName = "D:\\soft\\back\\doc\\image\\03.jpeg";
//        JSONObject result = upload(appId, appSecret, fileName);

//        String fileName = "D:\\soft\\back\\doc\\image\\aaa.mp4";
//        JSONObject result = null;
//        try {
//            result = uploadLong(appId, appSecret, fileName, "video", "视频标题", "视频描述");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(result);

        JSONObject jso = download(appId, appSecret, "EDr9cTn0TuY4wOVzm8xR_pAkJJ3dL5haX0ZHdmi1CCoSMMrXc48mU23628xKP0cf");
        System.out.println(jso);

    }

}
