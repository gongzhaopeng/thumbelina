package com.benben.wechat.mini.apiinvoker;

public class WechatPayRefundNotifyDescriptor {

    /**
     * Fields in the notification body.
     */
    final static public String FIELD_RETURN_CODE = "return_code";
    final static public String FIELD_REQ_INFO = "req_info";
    final static public String FIELD_REFUND_STATUS = "refund_status";
    final static public String FIELD_REFUND_ID = "refund_id";
    final static public String FIELD_OUT_REFUND_NO = "out_refund_no";

    final static public String RETURN_CODE_SUCCESS = "SUCCESS";
    final static public String RETURN_CODE_FAIL = "FAIL";

    final static public String REFUND_STATUS_SUCCESS = "SUCCESS";
    final static public String REFUND_STATUS_CHANGE = "CHANGE";
    final static public String REFUND_STATUS_REFUNDCLOSE = "REFUNDCLOSE";
}
