package com.benben.wechat.mini.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class CommonResponse {

    final static public Integer STATUS_CODE_SUCCESS = 0;

    private Integer statusCode;
    private String statusDetail;
}
