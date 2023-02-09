package com.tlf.wechat.mini.live;

import com.alibaba.fastjson.JSONObject;
import com.tlf.wechat.WeChatHttpUtil;
import com.tlf.wechat.mini.WeChatMiniUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeChatMiniLiveUtil {
    private static final Logger log = LoggerFactory.getLogger(WeChatMiniLiveUtil.class);


    /**
     * 设置直播参数
     *
     * @param name            直播间名称
     * @param coverImg        直播间背景图，图片规则：建议像素1080*1920，大小不超过2M
     * @param startTime       开播时间，需要在当前时间的10分钟后，并且开始时间不能在6个月后
     * @param endTime         开播结束时间，间隔不得短于30分钟，不得超过24小时
     * @param anchorName      主播昵称
     * @param anchorWechat    主播微信号，如果未实名认证，需要先前往“小程序直播”小程序进行实名验证, 小程序二维码链接：
     *                        // https://res.wx.qq.com/op_res/9rSix1dhHfK4rR049JL0PHJ7TpOvkuZ3mE0z7Ou_Etvjf-w1J_jVX0rZqeStLfwh
     * @param shareImg        分享图
     * @param feedsImg        购物直播频道封面图
     * @param type            直播间类型 【1: 推流，0：手机直播】
     * @param closeLike       是否关闭点赞 【0：开启，1：关闭】（若关闭，观众端将隐藏点赞按钮，直播开始后不允许开启）
     * @param closeGoods      是否关闭货架 【0：开启，1：关闭】（若关闭，观众端将隐藏商品货架，直播开始后不允许开启）
     * @param closeComment    是否关闭评论 【0：开启，1：关闭】（若关闭，观众端将隐藏评论入口，直播开始后不允许开启）
     * @param subAnchorWechat 主播副号微信号
     * @param createrWechat   创建者微信号，不传入则此直播间所有成员可见。传入则此房间仅创建者、管理员、超管、直播间主播可见
     * @param isFeedsPublic   是否开启官方收录 【1: 开启，0：关闭】，默认开启收录
     * @param closeReplay     是否关闭回放 【0：开启，1：关闭】默认关闭回放（直播开始后允许开启）
     * @param closeShare      是否关闭分享 【0：开启，1：关闭】默认开启分享（直播开始后不允许修改）
     * @param closeKf         是否关闭客服 【0：开启，1：关闭】 默认关闭客服（直播开始后允许开启）
     * @return
     */
    public static Map<String, Object> setParams(
            String name,
            String coverImg,
            Date startTime,
            Date endTime,
            String anchorName,
            String anchorWechat,
            String shareImg,
            String feedsImg,
            Integer type,
            Integer closeLike,
            Integer closeGoods,
            Integer closeComment,
            String subAnchorWechat,
            String createrWechat,
            Integer isFeedsPublic,
            Integer closeReplay,
            Integer closeShare,
            Integer closeKf
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("coverImg", coverImg);
        params.put("startTime", startTime.getTime() / 1000);
        params.put("endTime", endTime.getTime() / 1000);
        params.put("anchorName", anchorName);
        params.put("anchorWechat", anchorWechat);
        params.put("shareImg", shareImg);
        params.put("feedsImg", feedsImg);
        params.put("type", ObjectUtils.isNotEmpty(type) ? type : 0);
        params.put("closeLike", ObjectUtils.isNotEmpty(closeLike) ? closeLike : 0);
        params.put("closeGoods", ObjectUtils.isNotEmpty(closeGoods) ? closeGoods : 0);
        params.put("closeComment", ObjectUtils.isNotEmpty(closeComment) ? closeComment : 0);

        params.put("subAnchorWechat", ObjectUtils.isNotEmpty(subAnchorWechat) ? subAnchorWechat : null);
        params.put("createrWechat", ObjectUtils.isNotEmpty(createrWechat) ? createrWechat : null);
        params.put("isFeedsPublic", ObjectUtils.isNotEmpty(isFeedsPublic) ? isFeedsPublic : 1);
        params.put("closeReplay", ObjectUtils.isNotEmpty(closeReplay) ? closeReplay : 0);
        params.put("closeShare", ObjectUtils.isNotEmpty(closeShare) ? closeShare : 0);
        params.put("closeKf", ObjectUtils.isNotEmpty(closeKf) ? closeKf : 0);
        return params;
    }


    /**
     * 创建直播间
     *
     * @param appId
     * @param appSecret
     * @param params
     * @return
     */
    public static JSONObject create(String appId, String appSecret, Map<String, Object> params) {
//        String url = "https://api.weixin.qq.com/wxaapi/broadcast/room/create?access_token=";
        String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
        String host = "https://api.weixin.qq.com";
        String path = "/wxaapi/broadcast/room/create?access_token=" + access_token;

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "post", headers, null, params);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        return jsonObject;
    }


    /**
     * 获取直播列表
     *
     * @param appId
     * @param appSecret
     * @param start
     * @param limit
     * @return
     */
    public static JSONObject getLiveInfo(String appId, String appSecret, Integer start, Integer limit) {
        String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
        String host = "https://api.weixin.qq.com";
        String path = "/wxa/business/getliveinfo?access_token=" + access_token;
//        String url = "https://api.weixin.qq.com/wxa/business/getliveinfo?access_token=" + access_token;
        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("limit", limit);
        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "post", null, null, params);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        return jsonObject;

    }


    /**
     * 获取直播间分享二维码
     *
     * @param appId
     * @param appSecret
     * @param roomId
     * @return
     */
    public static JSONObject getShareCode(String appId, String appSecret, String roomId) {
//        String url = "https://api.weixin.qq.com/wxaapi/broadcast/room/getsharedcode?access_token=";
        String host = "https://api.weixin.qq.com";
        String path = "/wxaapi/broadcast/room/getsharedcode?access_token=";
        log.info("url={}", host + path);
        String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
        if (ObjectUtils.isEmpty(access_token)) {
            return null;
        }
        path = path + access_token + "&roomId=" + roomId;

        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "get", null, null, null);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        return jsonObject;
    }


    /**
     * 直播回放
     * @param appId
     * @param appSecret
     * @param roomId
     * @param start
     * @param limit
     * @return
     */
    public static JSONObject getLiveReplay(String appId, String appSecret, String roomId, Integer start, Integer limit) {
//        String url = "https://api.weixin.qq.com/wxa/business/getliveinfo?access_token=";
        String host = "https://api.weixin.qq.com";
        String path = "/wxa/business/getliveinfo?access_token=";
        log.info("url={}", host + path);
        String access_token = WeChatMiniUtil.getAccessToken(appId, appSecret);
        if (ObjectUtils.isEmpty(access_token)) {
            return null;
        }
        path = path + access_token;
        Map<String, Object> formData = new HashMap<>();
        formData.put("action", "get_replay");
        formData.put("room_id", roomId);
        formData.put("start", ObjectUtils.isNotEmpty(start) ? start : 0);
        formData.put("limit", ObjectUtils.isNotEmpty(limit) ? limit : 10);

        JSONObject jsonObject = WeChatHttpUtil.getBody(host, path, "post", null, null, formData);
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        return jsonObject;
    }


    public static void main(String[] args) {
        String appId = "wx4eb5de5673e10a8a";
        String appSecret = "b6733e39c3ad465bd78158633a9811b6";

        // 直播列表
        JSONObject jsonObject = getLiveInfo(appId, appSecret, 1, 10);
        System.out.println(jsonObject);

//        JSONObject liveReplay = getLiveReplay(appId, appSecret, "7", null, null);
//        System.out.println(liveReplay);


//        JSONObject shareCode = getShareCode(appId, appSecret, "3");
//        System.out.println(shareCode);

//        创建直接间
//        Map<String, Object> params = new HashMap<>();
//        String name = "大牛带你30天实现流量变量";
//        String coverImg = "iziuUyh522VK4hDgg0sQcvguXz9ZfqWl0mwyMnaoDuZsfQoeUHaeGojpDVK3XVOa";
//        int startTime = 1670233229;
//        int endTime = 1670236829;
//        String anchorName = "大魔王";
//        String anchorWechat = "soulmate-lingfeng";
//        String shareImg = "iziuUyh522VK4hDgg0sQcvguXz9ZfqWl0mwyMnaoDuZsfQoeUHaeGojpDVK3XVOa";
//        String feedsImg = "iziuUyh522VK4hDgg0sQcvguXz9ZfqWl0mwyMnaoDuZsfQoeUHaeGojpDVK3XVOa";
//        Integer type = 0;
//        Integer closeLike = 0;
//        Integer closeGoods = 0;
//        Integer closeComment = 0;
//        String subAnchorWechat = null;
//        String createrWechat = null;
//        Integer isFeedsPublic = 0;
//        Integer closeReplay = 0;
//        Integer closeShare = 0;
//        Integer closeKf = 0;
//        setParams(
//                name,
//                coverImg,
//                startTime,
//                endTime,
//                anchorName,
//                anchorWechat,
//                shareImg,
//                feedsImg,
//                type,
//                closeLike,
//                closeGoods,
//                closeComment,
//                subAnchorWechat,
//                createrWechat,
//                isFeedsPublic,
//                closeReplay,
//                closeShare,
//                closeKf
//        );
//
//        JSONObject jsonObject = create(appId, appSecret, params);


    }
}
