package com.benben.wechat.mini;

import com.benben.wechat.mini.component.JsonUtility;
import com.benben.wechat.mini.configuration.WechatPayConfiguration;
import com.benben.wechat.mini.util.WechatPayUtility;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class WechatPayUtilityTest {

    @Autowired
    private JsonUtility jsonUtility;

    @Autowired
    private WechatPayConfiguration wepayConfig;

    @Test
    public void parseXmlText() {

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
    public void toXmlText() {

        final Map<String, Object> request = Map.of(
                "return_code", "SUCCESS",
                "return_msg", "OK");

        final var xmlText = WechatPayUtility.toXmlText(request);

        log.info("xmlTest:\n{}", xmlText);
    }

    @Test
    public void sign() {

        final Map<String, Object> fieldsToSign = Map.of(
                "appid", "wxd930ea5d5a258f4f",
                "mch_id", "10000100",
                "device_info", "1000",
                "body", "test",
                "nonce_str", "ibuaiVcKdpRxkhJA");

        final var apiKey = "192006250b4c09247ec02edce69f6a2d";

        final var expectedSign =
                "9A0A8659F005D6984697E2CA0A9CF3B7";

        final var actualSign = WechatPayUtility.sign(
                fieldsToSign, apiKey, WechatPayUtility.SignType.MD5);

        Assert.assertEquals(expectedSign, actualSign);
    }

    @Test
    public void checkRefundSign() {

        final var jsonResp = "{\"transaction_id\":\"4200000322201905230936525580\",\"nonce_str\":\"v4qSc27fmyQGHnYY\",\"out_refund_no\":\"1558592445810\",\"sign\":\"A5F2AFA36FD49654969EC247DF0B0D8A\",\"return_msg\":\"OK\",\"mch_id\":\"1494681852\",\"refund_id\":\"50000400462019052309684108902\",\"cash_fee\":\"1\",\"out_trade_no\":\"5ce622da49d7446f9c20fa2a\",\"coupon_refund_fee\":\"0\",\"refund_channel\":\"\",\"appid\":\"wxc09ec1faa22cb365\",\"refund_fee\":\"1\",\"total_fee\":\"1\",\"result_code\":\"SUCCESS\",\"coupon_refund_count\":\"0\",\"cash_refund_fee\":\"1\",\"return_code\":\"SUCCESS\"}";

        final var parsedResp = jsonUtility.parseJsonText(jsonResp, Map.class);

        final var checkResult = WechatPayUtility.checkSign(parsedResp,
                wepayConfig.getApiKey(), WechatPayUtility.SignType.MD5);

        Assert.assertTrue(checkResult);
    }

    @Test
    public void decryptRefundNotify() {

        // TODO
    }
}
