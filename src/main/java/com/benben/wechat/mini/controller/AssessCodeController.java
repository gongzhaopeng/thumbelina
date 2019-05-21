package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.controller.exception.UserNotFoundException;
import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.AssessCodeService;
import com.benben.wechat.mini.service.UserUpdateLockService;
import com.benben.wechat.mini.service.WechatPayOrderService;
import com.benben.wechat.mini.util.WechatPayUtility;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/assesscodes")
public class AssessCodeController {

    final private UserRepository userRepository;
    final private UserUpdateLockService userUpdateLockService;
    final private AssessCodeService assessCodeService;
    final private WechatPayOrderService wechatPayOrderService;

    @Autowired
    public AssessCodeController(UserRepository userRepository,
                                UserUpdateLockService userUpdateLockService,
                                AssessCodeService assessCodeService,
                                WechatPayOrderService wechatPayOrderService) {

        this.userRepository = userRepository;
        this.userUpdateLockService = userUpdateLockService;
        this.assessCodeService = assessCodeService;
        this.wechatPayOrderService = wechatPayOrderService;
    }

    @PostMapping("/purchase")
    public PurchaseResp purchase(
            @Valid @RequestBody PurchaseReq purchaseReq) {

        final var openid = purchaseReq.getOpenid();

        if (!userRepository.existsById(openid)) {
            throw new UserNotFoundException();
        }

        return userUpdateLockService.doWithLock(openid, () -> {
            final var businessFields =
                    assessCodeService.constructWechatOrderBusinessFields(
                            openid, purchaseReq.amount);
            final var jsapiParams =
                    wechatPayOrderService.orderForJsapi(openid, businessFields);

            final var purchaseResp = new PurchaseResp();
            purchaseResp.setOrderId(businessFields.get(
                    WechatPayUnifiedorderInvoker.REQ_M_FIELD_OUT_TRADE_NO).toString());
            purchaseResp.setJsapiParams(jsapiParams);

            return purchaseResp;
        });
    }

    @ExceptionHandler(AssessCodeService.InvalidAssessCodePurchaseAmount.class)
    public CommonResponse invalidPurchaseAmountHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_INVALID_ASSESS_CODE_PURCHASE_AMOUNT);
        resp.setStatusDetail("Invalid assess-code purchase amount.");

        return resp;
    }

    @ExceptionHandler(WechatPayUnifiedorderInvoker.NoEnoughBalanceException.class)
    public CommonResponse wepayNoEnoughBalanceHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_WEPAY_NO_ENOUGH_BALANCE);
        resp.setStatusDetail("No enough balance in wechat:pay.");

        return resp;
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

    @Getter
    @Setter
    static class PurchaseResp extends CommonResponse {

        private String orderId;
        private WechatPayUtility.JsapiParams jsapiParams;
    }
}
