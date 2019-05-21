package com.benben.wechat.mini.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class CommonResponse {

    final static public Integer SC_SUCCESS = 0;

    final static public Integer SC_SERVICE_BUSY = 10000000;

    final static public Integer SC_WEPAY_NO_ENOUGH_BALANCE = 10000100;

    final static public Integer SC_INVALID_ASSESS_CODE_PURCHASE_AMOUNT = 10000200;

    final static public Integer SC_ASSESS_CODE_UNUSABLE = 10000300;

    private Integer statusCode = SC_SUCCESS;
    private String statusDetail;
}
