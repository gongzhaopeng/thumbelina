package com.benben.wechat.mini.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class CommonResponse {

    final static public Integer SC_SUCCESS = 0;

    final static public Integer SC_SERVICE_BUSY = 10000000;

    final static public Integer SC_WECHAT_INVALID_JS_CODE = 10000100;

    final static public Integer SC_WEPAY_NO_ENOUGH_BALANCE = 10000150;
    final static public Integer SC_WEPAY_REFUND_TRADE_OVERDUE = 10000151;
    final static public Integer SC_WEPAY_REFUND_NO_ENOUGH_BALANCE = 10000152;

    final static public Integer SC_INVALID_ASSESS_CODE_PURCHASE_AMOUNT = 10000200;

    final static public Integer SC_ASSESS_CODE_UNUSABLE = 10000300;
    final static public Integer SC_REFUND_OTHERS_ASSESS_CODE_DENY = 10000301;
    final static public Integer SC_ASSESS_CODE_NON_REFUNDABLE = 10000302;
    final static public Integer SC_ASSESS_CODE_CONCURRENT_REFUND_DENY = 10000303;

    private Integer statusCode = SC_SUCCESS;
    private String statusDetail;
}
