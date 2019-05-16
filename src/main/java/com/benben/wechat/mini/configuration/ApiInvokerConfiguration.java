package com.benben.wechat.mini.configuration;

import com.benben.wechat.mini.apiinvoker.WechatAuthCode2SessionInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayRefundInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.util.HostInformationExtractor;
import com.benben.wechat.mini.component.JsonUtility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiInvokerConfiguration {

    @Bean
    public WechatAuthCode2SessionInvoker
    wechatAuthCode2SessionInvoker(RestTemplate restTemplate,
                                  JsonUtility jsonUtility,
                                  WechatMiniProgramConfiguration wechatMiniConfig) {

        return new WechatAuthCode2SessionInvoker(
                restTemplate,
                jsonUtility,
                wechatMiniConfig.getAppId(),
                wechatMiniConfig.getAppSecret()
        );
    }

    @Bean
    public WechatPayUnifiedorderInvoker
    wechatPayUnifiedorderInvoker(RestTemplate restTemplate,
                                 JsonUtility jsonUtility,
                                 WechatMiniProgramConfiguration wechatMiniConfig,
                                 WechatPayConfiguration wechatPayConfig) {

        return new WechatPayUnifiedorderInvoker(
                restTemplate,
                jsonUtility,
                wechatMiniConfig.getAppId(),
                wechatPayConfig.getMchId(),
                wechatPayConfig.getApiKey(),
                wechatPayConfig.getNotifyUrl(),
                HostInformationExtractor.getHostIp());
    }

    @Bean
    public WechatPayRefundInvoker
    wechatPayRefundInvoker(RestTemplate restTemplate,
                           JsonUtility jsonUtility,
                           WechatMiniProgramConfiguration wechatMiniConfig,
                           WechatPayConfiguration wechatPayConfig) {

        return new WechatPayRefundInvoker(
                restTemplate,
                jsonUtility,
                wechatMiniConfig.getAppId(),
                wechatPayConfig.getMchId(),
                wechatPayConfig.getApiKey(),
                wechatPayConfig.getRefundNotifyUrl());
    }
}
