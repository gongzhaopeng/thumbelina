package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.util.WechatPayUtility;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/assesscodes")
public class AssessCodeController {

    @Autowired
    public AssessCodeController() {

    }

    @PostMapping("/purchase")
    public PurchaseResp purchase(
            @Valid @RequestBody PurchaseReq purchaseReq) {

        // TODO

        return null;
    }

    @Validated
    @Data
    static class PurchaseReq {

        @NotBlank
        private String openid;
        @NotNull
        @Positive
        private Integer amount;
    }

    @Valid
    @Getter
    static class PurchaseResp extends CommonResponse {

        private String orderId;
        private WechatPayUtility.JsapiParams jsapiParams;
    }
}
