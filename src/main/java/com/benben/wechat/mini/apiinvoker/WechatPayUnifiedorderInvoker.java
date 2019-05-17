package com.benben.wechat.mini.apiinvoker;

import com.benben.wechat.mini.component.JsonUtility;
import com.benben.wechat.mini.util.WechatPayUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WechatPayUnifiedorderInvoker {

    /**
     * Mandatory fields in the request body which will be auto-filled.
     */
    final static private String REQ_M_FIELD_APPID = "appid";
    final static private String REQ_M_FIELD_MCH_ID = "mch_id";
    final static private String REQ_M_FIELD_SIGN = "sign";
    final static private String REQ_M_FIELD_NOTIFY_URL = "notify_url";
    final static private String REQ_M_FIELD_SPBILL_CREATE_IP = "spbill_create_ip";
    final static private String REQ_M_FIELD_NONCE_STR = "nonce_str";

    /**
     * Optional fields in the request body which will be auto-filled.
     */
    final static private String REQ_O_FIELD_SIGN_TYPE = "sign_type";

    /**
     * Mandatory fields in the request body which must be provided by the caller.
     */
    final static public String REQ_M_FIELD_BODY = "body";
    final static public String REQ_M_FIELD_OUT_TRADE_NO = "out_trade_no";
    final static public String REQ_M_FIELD_TOTAL_FEE = "total_fee";
    final static public String REQ_M_FIELD_TRADE_TYPE = "trade_type";

    /**
     * Optional fields in the request body which must be provided by the caller.
     */
    final static public String REQ_O_FIELD_DEVICE_INFO = "device_info";
    final static public String REQ_O_FIELD_DETAIL = "detail";
    final static public String REQ_O_FIELD_ATTACH = "attach";
    final static public String REQ_O_FIELD_FEE_TYPE = "fee_type";
    final static public String REQ_O_FIELD_TIME_START = "time_start";
    final static public String REQ_O_FIELD_TIME_EXPIRE = "time_expire";
    final static public String REQ_O_FIELD_GOODS_TAG = "goods_tag";
    final static public String REQ_O_FIELD_PRODUCT_ID = "product_id";
    final static public String REQ_O_FIELD_LIMIT_PAY = "limit_pay";
    final static public String REQ_O_FIELD_OPENID = "openid";
    final static public String REQ_O_FIELD_RECEIPT = "receipt";
    final static public String REQ_O_FIELD_SCENE_INFO = "scene_info";

    /**
     * Fields in the response body.
     */
    final static public String RESP_FIELD_RETURN_CODE = "return_code";
    final static public String RESP_FIELD_RETURN_MSG = "return_msg";
    final static public String RESP_FIELD_APPID = "appid";
    final static public String RESP_FIELD_MCH_ID = "mch_id";
    final static public String RESP_FIELD_DEVICE_INFO = "device_info";
    final static public String RESP_FIELD_NONCE_STR = "nonce_str";
    final static public String RESP_FIELD_SIGN = "sign";
    final static public String RESP_FIELD_RESULT_CODE = "result_code";
    final static public String RESP_FIELD_ERROR_CODE = "err_code";
    final static public String RESP_FIELD_ERROR_CODE_DES = "err_code_des";
    final static public String RESP_FIELD_TRADE_TYPE = "trade_type";
    final static public String RESP_FIELD_PREPAY_ID = "prepay_id";
    final static public String RESP_FIELD_CODE_URL = "code_url";

    final static public String TRADE_TYPE_JSAPI = "JSAPI";

    final static private String RETURN_CODE_SUCCESS = "SUCCESS";
    final static private String RETURN_CODE_FAIL = "FAIL";

    final static private String RESULT_CODE_SUCCESS = "SUCCESS";
    final static private String RESULT_CODE_FAIL = "FAIL";

    final static private String ERR_CODE_NOTENOUGH = "NOTENOUGH";

    final static private String URL =
            "https://api.mch.weixin.qq.com/pay/unifiedorder";

    final private RestTemplate restTemplate;

    final private JsonUtility jsonUtility;

    final private String appId;
    final private String mchId;
    final private String apiKey;
    final private String notifyUrl;
    final private String spbillCreateIp;

    public WechatPayUnifiedorderInvoker(RestTemplate restTemplate,
                                        JsonUtility jsonUtility,
                                        String appId,
                                        String mchId,
                                        String apiKey,
                                        String notifyUrl,
                                        String spbillCreateIp) {

        this.restTemplate = restTemplate;
        this.jsonUtility = jsonUtility;

        this.appId = appId;
        this.mchId = mchId;
        this.apiKey = apiKey;
        this.notifyUrl = notifyUrl;
        this.spbillCreateIp = spbillCreateIp;

        log.info("WechatPayUnifiedorderInvoker configuration => "
                        + "appId: {}, mchId: {}, apiKey: {}, "
                        + "notifyUrl: {}, spbillCreateIp: {}",
                appId, mchId, apiKey, notifyUrl, spbillCreateIp);
    }

    /**
     * @param reqFields
     * @return
     * @throws NoEnoughBalanceException
     * @throws FatalExternalApiInvokeException
     */
    public Map<String, String> invoke(Map<String, Object> reqFields) {

        final var clonedReqFields = new HashMap<>(reqFields);
        clonedReqFields.put(REQ_M_FIELD_APPID, appId);
        clonedReqFields.put(REQ_M_FIELD_MCH_ID, mchId);
        clonedReqFields.put(REQ_M_FIELD_NOTIFY_URL, notifyUrl);
        clonedReqFields.put(REQ_M_FIELD_SPBILL_CREATE_IP, spbillCreateIp);
        clonedReqFields.put(REQ_M_FIELD_NONCE_STR,
                WechatPayUtility.generateNonceStr());

        final var sign = WechatPayUtility.sign(clonedReqFields,
                apiKey, WechatPayUtility.SignType.MD5);
        clonedReqFields.put(REQ_M_FIELD_SIGN, sign);

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        final var request = new HttpEntity<>(
                WechatPayUtility.toXmlText(reqFields), headers);

        final var response = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new FatalExternalApiInvokeException(
                    "The status-code in the response of wechat-api[pay.unifiedorder] is "
                            + response.getStatusCode().value() + ".\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields));
        }

        final var respBody = WechatPayUtility.parseXmlText(response.getBody());
        if (respBody == null) {
            throw new FatalExternalApiInvokeException(
                    "The body in the response of wechat-api[pay.unifiedorder] is null.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields));
        }

        if (!respBody.getOrDefault(RESP_FIELD_RETURN_CODE, RETURN_CODE_FAIL).equals(RETURN_CODE_SUCCESS)) {
            throw new FatalExternalApiInvokeException(
                    "The return-code in the response of wechat-api[pay.unifiedorder] isn't 'SUCCESS'.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                            + jsonUtility.toJsonString(respBody));
        }

        if (!WechatPayUtility.checkSign(new HashMap<>(respBody), apiKey, WechatPayUtility.SignType.MD5)) {
            throw new FatalExternalApiInvokeException(
                    "The sign in the response of wechat-api[pay.unifiedorder] is invalid.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                            + jsonUtility.toJsonString(respBody));
        }

        if (!respBody.getOrDefault(RESP_FIELD_RESULT_CODE, RESULT_CODE_FAIL).equals(RESULT_CODE_SUCCESS)) {

            final var errCode = respBody.getOrDefault(RESP_FIELD_ERROR_CODE, "UNDEFINED");

            if (errCode.equals(ERR_CODE_NOTENOUGH)) {
                throw new NoEnoughBalanceException("request-fields:\n"
                        + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                        + jsonUtility.toJsonString(respBody));
            }

            throw new FatalExternalApiInvokeException(
                    "The result-code in the response of wechat-api[pay.unifiedorder] is unacceptable.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                            + jsonUtility.toJsonString(respBody));
        }

        return respBody;
    }

    public static class NoEnoughBalanceException
            extends RuntimeException {

        NoEnoughBalanceException(String message) {

            super(message);
        }
    }
}
