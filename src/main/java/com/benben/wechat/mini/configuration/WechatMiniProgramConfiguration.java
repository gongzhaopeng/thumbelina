package com.benben.wechat.mini.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("wechat.mini-program")
@Data
public class WechatMiniProgramConfiguration {

    private String appId;

    private String appSecret;
}
