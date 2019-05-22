package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.FatalExternalApiInvokeException;
import com.benben.wechat.mini.apiinvoker.WechatPayRefundInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.controller.exception.AssessCodeNotFoundException;
import com.benben.wechat.mini.controller.exception.UserNotFoundException;
import com.benben.wechat.mini.model.AssessCode;
import com.benben.wechat.mini.model.AssessCodeRefund;
import com.benben.wechat.mini.repository.AssessCodeOrderRepository;
import com.benben.wechat.mini.repository.AssessCodeRepository;
import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.AssessCodeService;
import com.benben.wechat.mini.service.UserUpdateLockService;
import com.benben.wechat.mini.service.WechatPayOrderService;
import com.benben.wechat.mini.service.WechatPayRefundService;
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
    final private AssessCodeRepository assessCodeRepository;
    final private AssessCodeOrderRepository assessCodeOrderRepository;
    final private UserUpdateLockService userUpdateLockService;
    final private AssessCodeService assessCodeService;
    final private WechatPayOrderService wechatPayOrderService;
    final private WechatPayRefundService wechatPayRefundService;

    @Autowired
    public AssessCodeController(UserRepository userRepository,
                                AssessCodeRepository assessCodeRepository,
                                AssessCodeOrderRepository assessCodeOrderRepository,
                                UserUpdateLockService userUpdateLockService,
                                AssessCodeService assessCodeService,
                                WechatPayOrderService wechatPayOrderService,
                                WechatPayRefundService wechatPayRefundService) {

        this.userRepository = userRepository;
        this.assessCodeRepository = assessCodeRepository;
        this.assessCodeOrderRepository = assessCodeOrderRepository;
        this.userUpdateLockService = userUpdateLockService;
        this.assessCodeService = assessCodeService;
        this.wechatPayOrderService = wechatPayOrderService;
        this.wechatPayRefundService = wechatPayRefundService;
    }

    /**
     * @param purchaseReq
     * @return
     * @throws UserNotFoundException
     * @throws UserUpdateLockService.FailToAcquireUserUpdateLock
     * @throws AssessCodeService.InvalidAssessCodePurchaseAmount
     * @throws WechatPayUnifiedorderInvoker.NoEnoughBalanceException
     * @throws FatalExternalApiInvokeException
     */
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

    /**
     * @param refundReq
     * @return
     * @throws UserNotFoundException
     * @throws UserUpdateLockService.FailToAcquireUserUpdateLock
     * @throws AssessCodeNotFoundException
     * @throws RefundOthersAssessCodeDeny
     * @throws IllegalStateException
     * @throws AssessCodeService.NonRefundableException
     * @throws AssessCodeService.ConcurrentRefundDeny
     * @throws WechatPayRefundInvoker.TradeOverdueException
     * @throws WechatPayRefundInvoker.NoEnoughBalanceException
     * @throws FatalExternalApiInvokeException
     */
    @PostMapping("/refund")
    public RefundResp refund(
            @Valid @RequestBody RefundReq refundReq) {

        final var openid = refundReq.getOpenid();

        if (!userRepository.existsById(openid)) {
            throw new UserNotFoundException();
        }

        return userUpdateLockService.doWithLock(openid, () -> {

            final var assessCode =
                    assessCodeRepository.findById(refundReq.getAssessCode())
                            .orElseThrow(AssessCodeNotFoundException::new);

            if (!assessCode.getOwner().equals(openid)) {
                throw new RefundOthersAssessCodeDeny();
            }

            final var ownerUser = userRepository.findById(openid)
                    .orElseThrow(IllegalStateException::new);

            final var order = assessCodeOrderRepository.findById(assessCode.getOrderId())
                    .orElseThrow(IllegalStateException::new);
            final var refundItem = order.getItems().stream()
                    .filter(item -> item.getId().equals(assessCode.getOrderItemId()))
                    .findFirst().orElseThrow(IllegalStateException::new);

            final var businessFields =
                    assessCodeService.constructWechatRefundBusinessFields(refundItem, order);

            wechatPayRefundService.refund(businessFields);

            final var refundId =
                    businessFields.get(WechatPayRefundInvoker.REQ_M_FIELD_OUT_REFUND_NO).toString();

            refundItem.setRefundId(refundId);
            refundItem.setRefundState(AssessCodeRefund.State.REFUNDING);

            assessCode.setState(AssessCode.State.REFUNDING);
            assessCode.setRefundId(refundId);

            ownerUser.getAssessCode(assessCode.getCode())
                    .orElseThrow(IllegalStateException::new)
                    .setState(AssessCode.State.REFUNDING);

            userRepository.save(ownerUser);
            assessCodeRepository.save(assessCode);
            assessCodeOrderRepository.save(order);

            final var resp = new RefundResp();
            resp.setAssessCode(assessCode.getCode());
            resp.setRefundId(refundId);
            resp.setRefundFee(
                    (Integer) businessFields.get(WechatPayRefundInvoker.REQ_M_FIELD_REFUND_FEE));

            return resp;
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

    @ExceptionHandler(RefundOthersAssessCodeDeny.class)
    public CommonResponse refundOthersAssessCodeHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_REFUND_OTHERS_ASSESS_CODE_DENY);
        resp.setStatusDetail("Refunding others assess-code denied.");

        return resp;
    }

    @ExceptionHandler(AssessCodeService.NonRefundableException.class)
    public CommonResponse assessCodeNonRefundableHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_ASSESS_CODE_NON_REFUNDABLE);
        resp.setStatusDetail("Assess-code nonrefundable.");

        return resp;
    }

    @ExceptionHandler(AssessCodeService.ConcurrentRefundDeny.class)
    public CommonResponse concurrentRefundDenyHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_ASSESS_CODE_CONCURRENT_REFUND_DENY);
        resp.setStatusDetail("Assess-codes purchased together is not allowed to refund concurrently.");

        return resp;
    }

    @ExceptionHandler(WechatPayRefundInvoker.TradeOverdueException.class)
    public CommonResponse tradeOverdueHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_WEPAY_REFUND_TRADE_OVERDUE);
        resp.setStatusDetail("Refund trade overdue, please contact the service provider.");

        return resp;
    }

    @ExceptionHandler(WechatPayRefundInvoker.NoEnoughBalanceException.class)
    public CommonResponse refundNoEnoughBalanceHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_WEPAY_REFUND_NO_ENOUGH_BALANCE);
        resp.setStatusDetail("Out of Refund-Service, please contact the service provider.");

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

    @Validated
    @Data
    static class RefundReq {

        @NotBlank
        private String openid;

        @NotBlank
        private String assessCode;
    }

    @Getter
    @Setter
    static class RefundResp extends CommonResponse {

        private String assessCode;
        private String refundId;
        private Integer refundFee;
    }

    static class RefundOthersAssessCodeDeny
            extends RuntimeException {
    }
}
