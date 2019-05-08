package com.benben.wechat.mini.configuration;

import com.benben.wechat.mini.apiinvoker.WechatAuthCode2SessionInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.util.HostInformationExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiInvokerConfiguration {

    @Bean
    public WechatAuthCode2SessionInvoker
    wechatAuthCode2SessionInvoker(RestTemplate restTemplate,
                                  ObjectMapper objectMapper,
                                  WechatMiniProgramConfiguration wechatMiniConfig) {

        return new WechatAuthCode2SessionInvoker(
                restTemplate,
                objectMapper,
                wechatMiniConfig.getAppId(),
                wechatMiniConfig.getAppSecret()
        );
    }

    @Bean
    public WechatPayUnifiedorderInvoker
    wechatPayUnifiedorderInvoker(RestTemplate restTemplate,
                                 ObjectMapper objectMapper,
                                 WechatMiniProgramConfiguration wechatMiniConfig,
                                 WechatPayConfiguration wechatPayConfig) {

        return new WechatPayUnifiedorderInvoker(
                restTemplate,
                objectMapper,
                wechatMiniConfig.getAppId(),
                wechatPayConfig.getMchId(),
                wechatPayConfig.getApiKey(),
                wechatPayConfig.getNotifyUrl(),
                HostInformationExtractor.getHostIp());
    }
}
