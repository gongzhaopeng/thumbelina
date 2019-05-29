package com.benben.wechat.mini.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class CommonResponse {

    final static public Integer SC_SUCCESS = 0;

    final static public Integer SC_SERVICE_BUSY = 10000000;

    private Integer statusCode = SC_SUCCESS;
    private String statusDetail;
}
