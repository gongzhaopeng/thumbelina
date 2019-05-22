package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.WechatPayOrderNotifyDescriptor;
import com.benben.wechat.mini.apiinvoker.WechatPayRefundInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayRefundNotifyDescriptor;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.component.JsonUtility;
import com.benben.wechat.mini.configuration.WechatPayConfiguration;
import com.benben.wechat.mini.model.AssessCode;
import com.benben.wechat.mini.model.AssessCodeOrder;
import com.benben.wechat.mini.model.AssessCodeRefund;
import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.AssessCodeOrderRepository;
import com.benben.wechat.mini.repository.AssessCodeRefundRepository;
import com.benben.wechat.mini.repository.AssessCodeRepository;
import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.*;
import com.benben.wechat.mini.util.WechatPayUtility;
import com.benben.wechat.mini.util.WechatPayUtility.JsapiParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wechat/pay")
@Slf4j
public class WechatPayController {

    static final private int LOCK_ACQUIRE_RETRY = 3;

    final private WechatPayOrderService wechatPayOrderService;
    final private WechatPayRefundService wechatPayRefundService;
    final private JsonUtility jsonUtility;
    final private WechatPayConfiguration wechatPayConfig;
    final private AssessCodeOrderRepository orderRepository;
    final private UserRepository userRepository;
    final private AssessCodeRepository assessCodeRepository;
    final private AssessCodeRefundRepository assessCodeRefundRepository;
    final private UserUpdateLockService userUpdateLockService;
    final private AssessCodeCollLockService assessCodeCollLockService;
    final private AssessCodeService assessCodeService;

    @Autowired
    public WechatPayController(WechatPayOrderService wechatPayOrderService,
                               WechatPayRefundService wechatPayRefundService,
                               JsonUtility jsonUtility,
                               WechatPayConfiguration wechatPayConfig,
                               AssessCodeOrderRepository orderRepository,
                               UserRepository userRepository,
                               AssessCodeRepository assessCodeRepository,
                               AssessCodeRefundRepository assessCodeRefundRepository,
                               UserUpdateLockService userUpdateLockService,
                               AssessCodeCollLockService assessCodeCollLockService,
                               AssessCodeService assessCodeService) {

        this.wechatPayOrderService = wechatPayOrderService;
        this.wechatPayRefundService = wechatPayRefundService;
        this.jsonUtility = jsonUtility;
        this.wechatPayConfig = wechatPayConfig;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.assessCodeRepository = assessCodeRepository;
        this.assessCodeRefundRepository = assessCodeRefundRepository;
        this.userUpdateLockService = userUpdateLockService;
        this.assessCodeCollLockService = assessCodeCollLockService;
        this.assessCodeService = assessCodeService;
    }

    /**
     * @param notification
     * @return
     * @throws UserUpdateLockService.FailToAcquireUserUpdateLock
     * @throws AssessCodeCollLockService.FailToAcquireAssessCodeCollLock
     * @throws IllegalStateException
     * @throws AssessCodeService.FailToGenerateAssessCode
     */
    @PostMapping(
            path = "/notify",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE)
    public String processNotify(
            @RequestBody String notification) {

        log.info("Received wechat-pay notification: {}", notification);

        final var parsedNotify = WechatPayUtility.parseXmlText(notification);
        log.info("Parsed wechat-pay notification: {}", parsedNotify);

        if (!WechatPayOrderNotifyDescriptor.RETURN_CODE_SUCCESS.equals(
                parsedNotify.get(WechatPayOrderNotifyDescriptor.FIELD_RETURN_CODE))) {
            return constructRespToNotify("FAIL", "Invalid return_code");
        }

        if (!WechatPayUtility.checkSign(new HashMap<>(parsedNotify),
                wechatPayConfig.getApiKey(), WechatPayUtility.SignType.MD5)) {
            return constructRespToNotify("FAIL", "Invalid sign");
        }

        if (!WechatPayOrderNotifyDescriptor.RESULT_CODE_SUCCESS.equals(
                parsedNotify.get(WechatPayOrderNotifyDescriptor.FIELD_RESULT_CODE))) {
            return constructRespToNotify("SUCCESS", "OK");
        }

        final var outTradeNo = parsedNotify.get(WechatPayOrderNotifyDescriptor.FIELD_OUT_TRADE_NO);
        final var wepayTid = parsedNotify.get(WechatPayOrderNotifyDescriptor.FIELD_TRANSACTION_ID);
        final var optOrder = orderRepository.findById(outTradeNo);
        if (optOrder.isEmpty()) {
            return constructRespToNotify("FAIL",
                    String.format("Unrecognized order. Wepay-Transaction-ID: %s. out_trade_no: %s",
                            wepayTid, outTradeNo));
        }

        return userUpdateLockService.doWithLock(optOrder.get().getOwner(), LOCK_ACQUIRE_RETRY, () ->
                assessCodeCollLockService.doWithLock(LOCK_ACQUIRE_RETRY, () -> {

                    final var order = orderRepository.findById(outTradeNo)
                            .orElseThrow(IllegalStateException::new);
                    final var orderOwner = userRepository.findById(order.getOwner())
                            .orElseThrow(IllegalStateException::new);

                    if (order.getWepayNotify() != null) {
                        return constructRespToNotify("SUCCESS", "OK");
                    }

                    final var newAssessCodes =
                            assessCodeService.constructAssessCodesByOrder(order);

                    newAssessCodes.stream().map(ac -> {
                        final var userAssessCode = new User.AssessCode();
                        userAssessCode.setCode(ac.getCode());
                        userAssessCode.setCreateTime(ac.getCreateTime());
                        userAssessCode.setState(ac.getState());
                        return userAssessCode;
                    }).forEach(orderOwner::addAssessCode);

                    order.setState(AssessCodeOrder.State.PAID);
                    order.setWepayNotify(jsonUtility.toJsonString(parsedNotify));
                    order.setWepayNotifyTs(System.currentTimeMillis());
                    order.setWepayTid(wepayTid);

                    orderRepository.save(order);
                    assessCodeRepository.saveAll(newAssessCodes);
                    userRepository.save(orderOwner);

                    return constructRespToNotify("SUCCESS", "OK");
                })
        );
    }

    /**
     * @param notification
     * @return
     * @throws WechatPayUtility.RefundNotifyDecryptException
     * @throws UserUpdateLockService.FailToAcquireUserUpdateLock
     * @throws IllegalStateException
     */
    @PostMapping(
            path = "/notify/refund",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE)
    public String processRefundNotify(
            @RequestBody String notification) {

        log.info("Received wechat-pay refund notification: {}", notification);

        final var parsedNotify = WechatPayUtility.parseXmlText(notification);
        log.info("Parsed wechat-pay refund notification: {}", parsedNotify);

        if (!WechatPayRefundNotifyDescriptor.RETURN_CODE_SUCCESS.equals(
                parsedNotify.get(WechatPayRefundNotifyDescriptor.FIELD_RETURN_CODE))) {
            return constructRespToNotify("FAIL", "Invalid return_code");
        }

        final var reqInfo =
                parsedNotify.get(WechatPayRefundNotifyDescriptor.FIELD_REQ_INFO);
        final var decipheredFields =
                WechatPayUtility.decryptRefundNotify(reqInfo, wechatPayConfig.getApiKey());

        final var refundId =
                decipheredFields.get(WechatPayRefundNotifyDescriptor.FIELD_OUT_REFUND_NO);
        final var wepayRid =
                decipheredFields.get(WechatPayRefundNotifyDescriptor.FIELD_REFUND_ID);
        final var refundStatus =
                decipheredFields.get(WechatPayRefundNotifyDescriptor.FIELD_REFUND_STATUS);

        final var optRefund = assessCodeRefundRepository.findById(refundId);
        if (optRefund.isEmpty()) {
            return constructRespToNotify("FAIL",
                    String.format("Unrecognized refund. Wepay-Refund-ID: %s. Refund-ID: %s",
                            wepayRid, refundId));
        }

        final var orderId = optRefund.get().getOrderId();

        final var ownerOpenid = orderRepository.findById(orderId)
                .map(AssessCodeOrder::getOwner)
                .orElseThrow(IllegalStateException::new);

        return userUpdateLockService.doWithLock(ownerOpenid, LOCK_ACQUIRE_RETRY, () -> {

            final var refund = assessCodeRefundRepository.findById(refundId)
                    .orElseThrow(IllegalStateException::new);

            if (refund.getWepayNotify() != null) {
                return constructRespToNotify("SUCCESS", "OK");
            }

            final var order = orderRepository.findById(orderId)
                    .orElseThrow(IllegalStateException::new);
            final var ownerUser = userRepository.findById(ownerOpenid)
                    .orElseThrow(IllegalStateException::new);

            final var refundItem = order.getItems().stream()
                    .filter(item -> item.getId().equals(refund.getOrderItemId()))
                    .findFirst().orElseThrow(IllegalStateException::new);

            final var assessCode = assessCodeRepository.findById(refundItem.getAssessCode())
                    .orElseThrow(IllegalStateException::new);

            refund.setWepayNotify(jsonUtility.toJsonString(parsedNotify));
            refund.setWepayNotifyTs(System.currentTimeMillis());
            refund.setWepayRid(wepayRid);

            if (refundStatus.equals(
                    WechatPayRefundNotifyDescriptor.REFUND_STATUS_SUCCESS)) {

                refund.setState(AssessCodeRefund.State.REFUND_SUCCESS);

                refundItem.setRefundState(AssessCodeRefund.State.REFUND_SUCCESS);

                assessCode.setState(AssessCode.State.REFUNDED);

                ownerUser.getAssessCode(assessCode.getCode())
                        .orElseThrow(IllegalStateException::new)
                        .setState(AssessCode.State.REFUNDED);
            } else {

                refund.setState(AssessCodeRefund.State.REFUND_FAIL);

                refundItem.setRefundState(AssessCodeRefund.State.REFUND_FAIL);

                assessCode.setState(AssessCode.State.FRESH);

                ownerUser.getAssessCode(assessCode.getCode())
                        .orElseThrow(IllegalStateException::new)
                        .setState(AssessCode.State.FRESH);
            }

            userRepository.save(ownerUser);
            assessCodeRepository.save(assessCode);
            orderRepository.save(order);
            assessCodeRefundRepository.save(refund);

            return constructRespToNotify("SUCCESS", "OK");
        });
    }

    /**
     * TODO Remember to delete this api.
     *
     * @param openid
     * @return
     */
    @GetMapping("/trial/order/{openid}")
    public JsapiParams trialOrder(@PathVariable("openid") String openid) {

        final Map<String, Object> orderBusinessFields =
                Map.of(WechatPayUnifiedorderInvoker.REQ_M_FIELD_BODY, "Trial order ...",
                        WechatPayUnifiedorderInvoker.REQ_M_FIELD_OUT_TRADE_NO, System.currentTimeMillis(),
                        WechatPayUnifiedorderInvoker.REQ_M_FIELD_TOTAL_FEE, 1);

        log.info("Order-Business-Fields: {}",
                jsonUtility.toJsonString(orderBusinessFields));

        return wechatPayOrderService.orderForJsapi(openid, orderBusinessFields);
    }

    /**
     * TODO Remember to delete this api.
     *
     * @return
     */
    @GetMapping("/trial/refund/{out-trade-no}")
    public Map<String, String> trialRefund(
            @PathVariable("out-trade-no") String outTradeNo) {

        final Map<String, Object> refundBusinessFields =
                Map.of(WechatPayRefundInvoker.REQ_M_FIELD_OUT_TRADE_NO, outTradeNo,
                        WechatPayRefundInvoker.REQ_M_FIELD_OUT_REFUND_NO, System.currentTimeMillis(),
                        WechatPayRefundInvoker.REQ_M_FIELD_TOTAL_FEE, 1,
                        WechatPayRefundInvoker.REQ_M_FIELD_REFUND_FEE, 1,
                        WechatPayRefundInvoker.REQ_O_FIELD_REFUND_DESC, "Trial refund ...");

        return wechatPayRefundService.refund(refundBusinessFields);
    }

    @ExceptionHandler({
            UserUpdateLockService.FailToAcquireUserUpdateLock.class,
            AssessCodeCollLockService.FailToAcquireAssessCodeCollLock.class
    })
    public String failToAcquireLockHandler() {
        return constructRespToNotify("FAIL", "Service Busy");
    }

    @ExceptionHandler(AssessCodeService.FailToGenerateAssessCode.class)
    public String failToGenerateAssessCodeHandler() {
        return constructRespToNotify("FAIL", "Service Busy");
    }

    @ExceptionHandler(WechatPayUtility.RefundNotifyDecryptException.class)
    public String failToDecryptRefundNotifyHandler() {
        return constructRespToNotify("FAIL",
                "Fail to decrypt refund-notification.");
    }

    private String constructRespToNotify(String returnCode,
                                         String returnMsg) {

        return WechatPayUtility.toXmlText(Map.of(
                "return_code", returnCode,
                "return_msg", returnMsg
        ));
    }
}
