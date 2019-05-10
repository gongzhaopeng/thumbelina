package com.benben.wechat.mini;

import com.benben.wechat.mini.util.WechatPayUtility;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

@Slf4j
public class WechatPayUtilityTest {

    @Test
    public void basicParseXmlText() {

        final var mockedResponse =
                "<xml>\n" +
                        "  <appid><![CDATA[wx2421b1c4370ec43b]]></appid>\n" +
                        "  <attach><![CDATA[支付测试]]></attach>\n" +
                        "  <bank_type><![CDATA[CFT]]></bank_type>\n" +
                        "  <fee_type><![CDATA[CNY]]></fee_type>\n" +
                        "  <is_subscribe><![CDATA[Y]]></is_subscribe>\n" +
                        "  <mch_id><![CDATA[10000100]]></mch_id>\n" +
                        "  <nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>\n" +
                        "  <openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>\n" +
                        "  <out_trade_no><![CDATA[1409811653]]></out_trade_no>\n" +
                        "  <result_code><![CDATA[SUCCESS]]></result_code>\n" +
                        "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                        "  <sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>\n" +
                        "  <time_end><![CDATA[20140903131540]]></time_end>\n" +
                        "  <total_fee>1</total_fee>\n" +
                        "  <coupon_fee><![CDATA[10]]></coupon_fee>\n" +
                        "  <coupon_count><![CDATA[1]]></coupon_count>\n" +
                        "  <coupon_type><![CDATA[CASH]]></coupon_type>\n" +
                        "  <coupon_id><![CDATA[10000]]></coupon_id>\n" +
                        "  <coupon_fee><![CDATA[100]]></coupon_fee>\n" +
                        "  <trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                        "  <transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id>\n" +
                        "</xml>";

        final var parseResult =
                WechatPayUtility.parseXmlText(mockedResponse);

        Assert.assertEquals(parseResult.get("appid"),
                "wx2421b1c4370ec43b");
        Assert.assertEquals(parseResult.get("trade_type"),
                "JSAPI");
    }

    @Test
    public void basicToXmlText() {

        final Map<String, Object> request = Map.of(
                "return_code", "SUCCESS",
                "return_msg", "OK");

        final var xmlText = WechatPayUtility.toXmlText(request);

        log.info("xmlTest:\n{}", xmlText);
    }
}
