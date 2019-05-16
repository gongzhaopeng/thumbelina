package com.benben.wechat.mini.service;

import com.benben.wechat.mini.apiinvoker.FatalExternalApiInvokeException;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.configuration.WechatMiniProgramConfiguration;
import com.benben.wechat.mini.configuration.WechatPayConfiguration;
import com.benben.wechat.mini.util.WechatPayUtility;
import com.benben.wechat.mini.util.WechatPayUtility.JsapiParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WechatPayOrderService {

    final private WechatPayUnifiedorderInvoker unifiedorderInvoker;
    final private WechatMiniProgramConfiguration wechatMiniConfig;
    final private WechatPayConfiguration wechatPayConfig;

    @Autowired
    public WechatPayOrderService(
            WechatPayUnifiedorderInvoker unifiedorderInvoker,
            WechatMiniProgramConfiguration wechatMiniConfig,
            WechatPayConfiguration wechatPayConfig) {

        this.unifiedorderInvoker = unifiedorderInvoker;
        this.wechatMiniConfig = wechatMiniConfig;
        this.wechatPayConfig = wechatPayConfig;
    }

    /**
     * @param openid
     * @param orderBusinessFields
     * @return
     * @throws WechatPayUnifiedorderInvoker.NoEnoughBalanceException
     * @throws FatalExternalApiInvokeException
     */
    public JsapiParams orderForJsapi(
            String openid,
            Map<String, Object> orderBusinessFields) {

        final var reqFields = new HashMap<>(orderBusinessFields);
        reqFields.put(WechatPayUnifiedorderInvoker.REQ_M_FIELD_TRADE_TYPE,
                WechatPayUnifiedorderInvoker.TRADE_TYPE_JSAPI);
        reqFields.put(WechatPayUnifiedorderInvoker.REQ_O_FIELD_OPENID, openid);

        final var respBody = unifiedorderInvoker.invoke(reqFields);

        return WechatPayUtility.generateJsapiParams(
                wechatMiniConfig.getAppId(),
                Optional.ofNullable(respBody.get(WechatPayUnifiedorderInvoker.RESP_FIELD_PREPAY_ID))
                        .orElseThrow(),
                wechatPayConfig.getApiKey());
    }
}
