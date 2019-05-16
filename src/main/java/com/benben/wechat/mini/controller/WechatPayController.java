package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.WechatPayRefundInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.component.JsonUtility;
import com.benben.wechat.mini.service.WechatPayOrderService;
import com.benben.wechat.mini.service.WechatPayRefundService;
import com.benben.wechat.mini.util.WechatPayUtility;
import com.benben.wechat.mini.util.WechatPayUtility.JsapiParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wechat/pay")
@Slf4j
public class WechatPayController {

    static final private
    Map<String, Object> SUCCESS_RESPONSE_TO_NOTIFY =
            Map.of("return_code", "SUCCESS",
                    "return_msg", "OK");

    final private WechatPayOrderService wechatPayOrderService;
    final private WechatPayRefundService wechatPayRefundService;
    final private JsonUtility jsonUtility;

    @Autowired
    public WechatPayController(WechatPayOrderService wechatPayOrderService,
                               WechatPayRefundService wechatPayRefundService,
                               JsonUtility jsonUtility) {

        this.wechatPayOrderService = wechatPayOrderService;
        this.wechatPayRefundService = wechatPayRefundService;
        this.jsonUtility = jsonUtility;
    }

    @PostMapping(
            path = "/notify",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE)
    public String processNotify(
            @RequestBody String notification) {

        log.info("Received wechat-pay notification: {}", notification);

        log.info("Parsed wechat-pay notification: {}",
                WechatPayUtility.parseXmlText(notification));

        // TODO Remember to implement the business logic.

        return WechatPayUtility.toXmlText(SUCCESS_RESPONSE_TO_NOTIFY);
    }

    @PostMapping(
            path = "/notify/refund",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE)
    public String processRefundNotify(
            @RequestBody String notification) {

        log.info("Received wechat-pay refund notification: {}", notification);

        log.info("Parsed wechat-pay refund notification: {}",
                WechatPayUtility.parseXmlText(notification));

        // TODO Remember to implement the business logic.

        return WechatPayUtility.toXmlText(SUCCESS_RESPONSE_TO_NOTIFY);
    }

    /**
     * TODO Remember to delete this api.
     *
     * @param openid
     * @return
     */
    @GetMapping("/trial/order/{openid}")
    public JsapiParams trialOrder(@PathVariable("openid") String openid) {

        final Map<String, Object> orderBusinessFields =
                Map.of(WechatPayUnifiedorderInvoker.REQ_M_FIELD_BODY, "Trial order ...",
                        WechatPayUnifiedorderInvoker.REQ_M_FIELD_OUT_TRADE_NO, System.currentTimeMillis(),
                        WechatPayUnifiedorderInvoker.REQ_M_FIELD_TOTAL_FEE, 1);

        log.info("Order-Business-Fields: {}",
                jsonUtility.toJsonString(orderBusinessFields));

        return wechatPayOrderService.orderForJsapi(openid, orderBusinessFields);
    }

    /**
     * TODO Remember to delete this api.
     *
     * @return
     */
    @GetMapping("/trial/refund/{out-trade-no}")
    public Map<String, String> trialRefund(
            @PathVariable("out-trade-no") String outTradeNo) {

        final Map<String, Object> refundBusinessFields =
                Map.of(WechatPayRefundInvoker.REQ_M_FIELD_OUT_TRADE_NO, outTradeNo,
                        WechatPayRefundInvoker.REQ_M_FIELD_OUT_REFUND_NO, System.currentTimeMillis(),
                        WechatPayRefundInvoker.REQ_M_FIELD_TOTAL_FEE, 1,
                        WechatPayRefundInvoker.REQ_M_FIELD_REFUND_FEE, 1,
                        WechatPayRefundInvoker.REQ_O_FIELD_REFUND_DESC, "Trial refund ...");

        return wechatPayRefundService.refund(refundBusinessFields);
    }
}
