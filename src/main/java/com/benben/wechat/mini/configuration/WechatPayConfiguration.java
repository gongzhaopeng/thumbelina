package com.benben.wechat.mini.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("wechat.pay")
@Data
public class WechatPayConfiguration {

    private String mchId;

    private String apiKey;

    private String certPath;

    private String certPassword;

    private String notifyUrl;

    private String refundNotifyUrl;

    private Integer refundRetryTimes;

    private Integer refundRetryDelay;
}
