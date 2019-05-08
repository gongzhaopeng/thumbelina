package com.benben.wechat.mini.apiinvoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

public class WechatAuthCode2SessionInvoker {

    final static private String URL =
            "https://api.weixin.qq.com/sns/jscode2session";

    final static private String GRANT_TYPE = "authorization_code";

    final static private int ERRCODE_SUCCESS = 0;
    final static private int ERRCODE_INVALID_CODE = 40029;

    final private RestTemplate restTemplate;

    final private ObjectMapper objectMapper;

    final private String appId;
    final private String appSecret;

    public WechatAuthCode2SessionInvoker(RestTemplate restTemplate,
                                         ObjectMapper objectMapper,
                                         String appId,
                                         String appSecret) {

        this.restTemplate = restTemplate;

        this.objectMapper = objectMapper;

        this.appId = appId;
        this.appSecret = appSecret;
    }

    public Return invoke(String jsCode) {

        final var uriComponents = UriComponentsBuilder.fromHttpUrl(URL)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", jsCode)
                .queryParam("grant_type", GRANT_TYPE)
                .build();

        final var ret = Optional.ofNullable(
                restTemplate.getForObject(uriComponents.toUri(), Return.class));

        return ret.map(r -> {

            if (r.errcode == ERRCODE_SUCCESS) {
                return r;
            } else if (r.errcode == ERRCODE_INVALID_CODE) {
                throw new InvalidJsCodeException();
            } else {

                var exceptionMsg = "";
                try {
                    exceptionMsg = objectMapper.writeValueAsString(r);
                } catch (Exception e) {
                    // Damned nonsense, but it is java again.
                    throw new RuntimeException(e);
                }

                throw new FatalExternalApiInvokeException(exceptionMsg);
            }
        }).orElseThrow(() -> new FatalExternalApiInvokeException(
                "The response of wechat-api[auth.code2Session] is null."));
    }

    @Data
    private static class Return {

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
