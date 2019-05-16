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
public class WechatPayRefundInvoker {

    /**
     * Mandatory fields in the request body which will be auto-filled.
     */
    final static private String REQ_M_FIELD_APPID = "appid";
    final static private String REQ_M_FIELD_MCH_ID = "mch_id";
    final static private String REQ_M_FIELD_NONCE_STR = "nonce_str";
    final static private String REQ_M_FIELD_SIGN = "sign";

    /**
     * Optional fields in the request body which will be auto-filled.
     */
    final static private String REQ_O_FIELD_SIGN_TYPE = "sign_type";
    final static private String REQ_M_FIELD_NOTIFY_URL = "notify_url";

    /**
     * Mandatory fields in the request body which must be provided by the caller.
     */
    final static public String REQ_M_FIELD_TRANSACTION_ID = "transaction_id";
    final static public String REQ_M_FIELD_OUT_TRADE_NO = "out_trade_no";
    final static public String REQ_M_FIELD_OUT_REFUND_NO = "out_refund_no";
    final static public String REQ_M_FIELD_TOTAL_FEE = "total_fee";
    final static public String REQ_M_FIELD_REFUND_FEE = "refund_fee";

    /**
     * Optional fields in the request body which must be provided by the caller.
     */
    final static public String REQ_O_FIELD_REFUND_FEE_TYPE = "refund_fee_type";
    final static public String REQ_O_FIELD_REFUND_DESC = "refund_desc";
    final static public String REQ_O_FIELD_REFUND_ACCOUNT = "refund_account";

    /**
     * Fields in the response body.
     */
    final static public String RESP_FIELD_RETURN_CODE = "return_code";
    final static public String RESP_FIELD_RETURN_MSG = "return_msg";
    final static public String RESP_FIELD_RESULT_CODE = "result_code";
    final static public String RESP_FIELD_ERROR_CODE = "err_code";
    final static public String RESP_FIELD_ERROR_CODE_DES = "err_code_des";
    final static public String RESP_FIELD_APPID = "appid";
    final static public String RESP_FIELD_MCH_ID = "mch_id";
    final static public String RESP_FIELD_NONCE_STR = "nonce_str";
    final static public String RESP_FIELD_SIGN = "sign";
    final static public String RESP_FIELD_TRANSACTION_ID = "transaction_id";
    final static public String RESP_FIELD_OUT_TRADE_NO = "out_trade_no";
    final static public String RESP_FIELD_OUT_REFUND_NO = "out_refund_no";
    final static public String RESP_FIELD_REFUND_ID = "refund_id";
    final static public String RESP_FIELD_REFUND_FEE = "refund_fee";
    final static public String RESP_FIELD_SETTLEMENT_REFUND_FEE = "settlement_refund_fee";
    final static public String RESP_FIELD_TOTAL_FEE = "total_fee";
    final static public String RESP_FIELD_SETTLEMENT_TOTAL_FEE = "settlement_total_fee";
    final static public String RESP_FIELD_FEE_TYPE = "fee_type";
    final static public String RESP_FIELD_CASH_FEE = "cash_fee";
    final static public String RESP_FIELD_CASH_FEE_TYPE = "cash_fee_type";
    final static public String RESP_FIELD_CASH_REFUND_FEE = "cash_refund_fee";
    // coupon_type_$n => 代金券类型
    final static public String RESP_FIELD_COUPON_FEFUND_FEE = "coupon_refund_fee";
    // coupon_refund_fee_$n => 单个代金券退款金额
    final static public String RESP_FIELD_COUPON_FEFUND_COUNT = "coupon_refund_count";
    // coupon_refund_id_$n => 退款代金券ID

    final static private String RETURN_CODE_SUCCESS = "SUCCESS";
    final static private String RETURN_CODE_FAIL = "FAIL";

    final static private String RESULT_CODE_SUCCESS = "SUCCESS";
    final static private String RESULT_CODE_FAIL = "FAIL";

    final static private String ERR_CODE_SYSTEMERROR = "SYSTEMERROR";
    final static private String ERR_CODE_BIZERR_NEED_RETRY = "BIZERR_NEED_RETRY";
    final static private String ERR_CODE_TRADE_OVERDUE = "TRADE_OVERDUE";
    final static private String ERR_CODE_NOTENOUGH = "NOTENOUGH";
    // TODO

    final static private String URL =
            "https://api.mch.weixin.qq.com/secapi/pay/refund";

    final private RestTemplate restTemplate;

    final private JsonUtility jsonUtility;

    final private String appId;
    final private String mchId;
    final private String apiKey;
    final private String notifyUrl;

    public WechatPayRefundInvoker(RestTemplate restTemplate,
                                  JsonUtility jsonUtility,
                                  String appId,
                                  String mchId,
                                  String apiKey,
                                  String notifyUrl) {

        this.restTemplate = restTemplate;
        this.jsonUtility = jsonUtility;

        this.appId = appId;
        this.mchId = mchId;
        this.apiKey = apiKey;
        this.notifyUrl = notifyUrl;

        log.info("WechatPayRefundInvoker configuration => "
                        + "appId: {}, mchId: {}, apiKey: {}, "
                        + "notifyUrl: {}",
                appId, mchId, apiKey, notifyUrl);
    }

    /**
     * @param reqFields
     * @return
     * @throws NeedRetryException
     * @throws TradeOverdueException
     * @throws NoEnoughBalanceException
     * @throws FatalExternalApiInvokeException
     */
    public Map<String, String> invoke(Map<String, Object> reqFields) {

        final var clonedReqFields = new HashMap<>(reqFields);
        clonedReqFields.put(REQ_M_FIELD_APPID, appId);
        clonedReqFields.put(REQ_M_FIELD_MCH_ID, mchId);
        clonedReqFields.put(REQ_M_FIELD_NONCE_STR,
                WechatPayUtility.generateNonceStr());
        clonedReqFields.put(REQ_M_FIELD_NOTIFY_URL, notifyUrl);

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
                    "The status-code in the response of wechat-api[pay.refund] is "
                            + response.getStatusCode().value() + ".\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields));
        }

        final var respBody = WechatPayUtility.parseXmlText(response.getBody());
        if (respBody == null) {
            throw new FatalExternalApiInvokeException(
                    "The body in the response of wechat-api[pay.refund] is null.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields));
        }

        if (!respBody.getOrDefault(RESP_FIELD_RETURN_CODE, RETURN_CODE_FAIL).equals(RETURN_CODE_SUCCESS)) {
            throw new FatalExternalApiInvokeException(
                    "The return-code in the response of wechat-api[pay.refund] isn't 'SUCCESS'.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                            + jsonUtility.toJsonString(respBody));
        }

        if (!respBody.getOrDefault(RESP_FIELD_RESULT_CODE, RESULT_CODE_FAIL).equals(RESULT_CODE_SUCCESS)) {

            final var errCode = respBody.getOrDefault(RESP_FIELD_ERROR_CODE, "UNDEFINED");

            if (errCode.equals(ERR_CODE_SYSTEMERROR) ||
                    errCode.equals(ERR_CODE_BIZERR_NEED_RETRY)) {

                throw new NeedRetryException("request-fields:\n"
                        + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                        + jsonUtility.toJsonString(respBody));
            }

            if (errCode.equals(ERR_CODE_TRADE_OVERDUE)) {

                throw new TradeOverdueException("request-fields:\n"
                        + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                        + jsonUtility.toJsonString(respBody));
            }

            if (errCode.equals(ERR_CODE_NOTENOUGH)) {
                throw new NoEnoughBalanceException("request-fields:\n"
                        + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                        + jsonUtility.toJsonString(respBody));
            }

            throw new FatalExternalApiInvokeException(
                    "The result-code in the response of wechat-api[pay.refund] is unacceptable.\nrequest-fields:\n"
                            + jsonUtility.toJsonString(reqFields) + "\nresponse-body:\n"
                            + jsonUtility.toJsonString(respBody));
        }

        return respBody;
    }

    /**
     * Need Retry with the same request parameters.
     */
    public static class NeedRetryException
            extends RuntimeException {

        NeedRetryException(String message) {

            super(message);
        }
    }

    /**
     * Over the refund-time-limit.
     */
    public static class TradeOverdueException
            extends RuntimeException {

        TradeOverdueException(String message) {

            super(message);
        }
    }

    /**
     * Balance is not enough.
     */
    public static class NoEnoughBalanceException
            extends RuntimeException {

        NoEnoughBalanceException(String message) {

            super(message);
        }
    }
}
