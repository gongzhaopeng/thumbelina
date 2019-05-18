package com.benben.wechat.mini.apiinvoker;

import com.benben.wechat.mini.component.JsonUtility;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
public class WechatAuthCode2SessionInvoker {

    final static private String URL =
            "https://api.weixin.qq.com/sns/jscode2session";

    final static private String GRANT_TYPE = "authorization_code";

    final static private int ERRCODE_SUCCESS = 0;
    final static private int ERRCODE_INVALID_CODE = 40029;

    final private RestTemplate restTemplate;

    final private JsonUtility jsonUtility;

    final private String appId;
    final private String appSecret;

    public WechatAuthCode2SessionInvoker(RestTemplate restTemplate,
                                         JsonUtility jsonUtility,
                                         String appId,
                                         String appSecret) {

        this.restTemplate = restTemplate;

        this.jsonUtility = jsonUtility;

        this.appId = appId;
        this.appSecret = appSecret;

        log.info("WechatAuthCode2SessionInvoker configuration => "
                        + "appId: {}, appSecret: {}",
                appId, appSecret);
    }

    /**
     * @param jsCode
     * @return
     * @throws InvalidJsCodeException
     * @throws FatalExternalApiInvokeException
     */
    public Return invoke(String jsCode) {

        final var uriComponents = UriComponentsBuilder.fromHttpUrl(URL)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", jsCode)
                .queryParam("grant_type", GRANT_TYPE)
                .build();

        final var ret = Optional.ofNullable(
                restTemplate.getForObject(uriComponents.toUri(), String.class))
                .map(retText -> jsonUtility.parseJsonText(retText, Return.class));

        return ret.map(r -> {

            if (r.errcode == ERRCODE_SUCCESS) {
                return r;
            } else if (r.errcode == ERRCODE_INVALID_CODE) {
                throw new InvalidJsCodeException();
            } else {
                throw new FatalExternalApiInvokeException(
                        jsonUtility.toJsonString(r));
            }
        }).orElseThrow(() -> new FatalExternalApiInvokeException(
                "The response of wechat-api[auth.code2Session] is null."));
    }

    @Data
    public static class Return {

        private String openid;
        private String sessionKey;
        private String unionid;

        private Long errcode;
        private String errmsg;
    }

    public static class InvalidJsCodeException
            extends RuntimeException {

        InvalidJsCodeException() {

            super("The js-code provided for invoking wechat-api[auth.code2Session] is invalid.");
        }
    }
}
