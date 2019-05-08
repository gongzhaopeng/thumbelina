package com.benben.wechat.mini.util;

import java.util.Map;

public class WechatPayUtility {

    static public String sign(
            Map<String, Object> fieldsToSign,
            String apiKey,
            SignType signType) {

        // TODO
        return null;
    }

    public enum SignType {
        MD5,
//        HMAC_SHA256
    }
}
