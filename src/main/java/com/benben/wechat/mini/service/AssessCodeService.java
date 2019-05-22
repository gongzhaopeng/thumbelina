package com.benben.wechat.mini.service;

import com.benben.wechat.mini.apiinvoker.WechatPayRefundInvoker;
import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.model.AssessCode;
import com.benben.wechat.mini.model.AssessCodeOrder;
import com.benben.wechat.mini.model.AssessCodeRefund;
import com.benben.wechat.mini.repository.AssessCodeOrderRepository;
import com.benben.wechat.mini.repository.AssessCodeRefundRepository;
import com.benben.wechat.mini.repository.AssessCodeRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AssessCodeService {

    final static private List<Integer> AMOUNT_TO_FEE =
            List.of(0, 1, 2, 3);

    final static private String ASSESS_CODE_WECHAT_ORDER_BODY =
            "本本教育:志愿填报系统:测评码";

    final static private int CODE_LENGTH = 8;
    final static private int MAX_CODE_GENERATE_RETRY = 3;

    final private AssessCodeOrderRepository assessCodeOrderRepository;
    final private AssessCodeRepository assessCodeRepository;
    final private AssessCodeRefundRepository assessCodeRefundRepository;

    @Autowired
    public AssessCodeService(
            AssessCodeOrderRepository assessCodeOrderRepository,
            AssessCodeRepository assessCodeRepository,
            AssessCodeRefundRepository assessCodeRefundRepository) {

        this.assessCodeOrderRepository = assessCodeOrderRepository;
        this.assessCodeRepository = assessCodeRepository;
        this.assessCodeRefundRepository = assessCodeRefundRepository;
    }

    /**
     * @param ownerOpenid
     * @param amount
     * @return
     * @throws InvalidAssessCodePurchaseAmount
     */
    public Map<String, Object> constructWechatOrderBusinessFields(
            String ownerOpenid, Integer amount) {

        return Optional.ofNullable(AMOUNT_TO_FEE.get(amount)).map(fee -> {

            final var assessCodeOrder = generateAssessCodeOrder(
                    ownerOpenid, amount, fee);
            final var savedAssessCodeOrder =
                    assessCodeOrderRepository.save(assessCodeOrder);

            return Map.<String, Object>of(
                    WechatPayUnifiedorderInvoker.REQ_M_FIELD_BODY, ASSESS_CODE_WECHAT_ORDER_BODY,
                    WechatPayUnifiedorderInvoker.REQ_M_FIELD_OUT_TRADE_NO, savedAssessCodeOrder.getId(),
                    WechatPayUnifiedorderInvoker.REQ_M_FIELD_TOTAL_FEE, fee
            );
        }).orElseThrow(InvalidAssessCodePurchaseAmount::new);
    }

    /**
     * @param refundItem
     * @param order
     * @return
     * @throws NonRefundableException
     * @throws ConcurrentRefundDeny
     */
    public Map<String, Object> constructWechatRefundBusinessFields(
            AssessCodeOrder.Item refundItem, AssessCodeOrder order) {

        if (refundItem.getRefundId() != null &&
                refundItem.getRefundState() != AssessCodeRefund.State.REFUND_FAIL) {
            throw new NonRefundableException();
        }

        if (order.getItems().stream().anyMatch(
                item -> AssessCodeRefund.State.REFUNDING == item.getRefundState())) {
            throw new ConcurrentRefundDeny();
        }

        final var refundFee = computeRefundFee(order);

        final var refund = generateAssessCodeRefund(order.getId(), refundItem.getId(), refundFee);
        final var savedRefund = assessCodeRefundRepository.save(refund);

        return Map.of(
                WechatPayRefundInvoker.REQ_M_FIELD_OUT_TRADE_NO, order.getId(),
                WechatPayRefundInvoker.REQ_M_FIELD_OUT_REFUND_NO, savedRefund.getId(),
                WechatPayRefundInvoker.REQ_M_FIELD_TOTAL_FEE, order.getFee(),
                WechatPayRefundInvoker.REQ_M_FIELD_REFUND_FEE, refundFee);
    }

    /**
     * @param order
     * @return
     * @throws FailToGenerateAssessCode
     */
    public List<AssessCode> constructAssessCodesByOrder(AssessCodeOrder order) {

        final var codes = generateCodes(order.getItems().size(), 0);

        return IntStream.range(0, codes.size()).mapToObj(i -> {

            final var assessCode = new AssessCode();
            assessCode.setCode(codes.get(i));
            assessCode.setCreateTime(System.currentTimeMillis());
            assessCode.setState(AssessCode.State.FRESH);
            assessCode.setOwner(order.getOwner());
            assessCode.setOrderId(order.getId());
            assessCode.setOrderItemId(order.getItems().get(i).getId());

            return assessCode;
        }).collect(Collectors.toList());
    }

    private AssessCodeOrder generateAssessCodeOrder(
            String ownerOpenid, Integer amount, Integer fee) {

        final var assessCodeOrder = new AssessCodeOrder();
        assessCodeOrder.setCreateTime(System.currentTimeMillis());
        assessCodeOrder.setOwner(ownerOpenid);
        assessCodeOrder.setFee(fee);
        assessCodeOrder.setState(AssessCodeOrder.State.UNPAID);

        final var items = IntStream.range(0, amount).mapToObj(i -> {
            final var item = new AssessCodeOrder.Item();
            item.setId(new ObjectId().toString());

            return item;
        }).collect(Collectors.toList());
        assessCodeOrder.setItems(items);

        return assessCodeOrder;
    }

    private AssessCodeRefund generateAssessCodeRefund(
            String orderId, String orderItemId, Integer fee) {

        final var assessCodeRefund = new AssessCodeRefund();
        assessCodeRefund.setCreateTime(System.currentTimeMillis());
        assessCodeRefund.setOrderId(orderId);
        assessCodeRefund.setOrderItemId(orderItemId);
        assessCodeRefund.setFee(fee);
        assessCodeRefund.setState(AssessCodeRefund.State.REFUNDING);

        return assessCodeRefund;
    }

    /**
     * @return
     * @throws FailToGenerateAssessCode
     */
    private List<String> generateCodes(int amount, int retryCount) {

        if (retryCount > MAX_CODE_GENERATE_RETRY) {
            throw new FailToGenerateAssessCode();
        }

        final var rawCodes = IntStream.range(0, amount)
                .mapToObj(i -> RandomStringUtils.randomAlphanumeric(CODE_LENGTH))
                .collect(Collectors.toList());

        final var occupiedAssessCodes =
                IteratorUtils.toList(
                        assessCodeRepository.findAllById(rawCodes).iterator());

        final var validCodes = rawCodes.stream().filter(rawCode ->
                occupiedAssessCodes.stream().noneMatch(ac ->
                        ac.getCode().equals(rawCode)))
                .collect(Collectors.toList());

        if (validCodes.size() < amount) {
            validCodes.addAll(generateCodes(
                    amount - validCodes.size(), retryCount + 1));
        }

        return validCodes;
    }

    private Integer computeRefundFee(AssessCodeOrder order) {

        final var refundableCount = (int) order.getItems().stream().filter(item ->
                item.getRefundId() == null ||
                        AssessCodeRefund.State.REFUND_FAIL == item.getRefundState())
                .count();

        assert refundableCount > 0;

        return AMOUNT_TO_FEE.get(refundableCount) -
                AMOUNT_TO_FEE.get(refundableCount - 1);
    }

    public static class InvalidAssessCodePurchaseAmount
            extends RuntimeException {
    }

    public static class FailToGenerateAssessCode
            extends RuntimeException {
    }

    public static class NonRefundableException
            extends RuntimeException {
    }

    public static class ConcurrentRefundDeny
            extends RuntimeException {
    }
}
