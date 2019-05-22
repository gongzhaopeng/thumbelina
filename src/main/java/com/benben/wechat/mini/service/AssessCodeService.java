package com.benben.wechat.mini.service;

import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.model.AssessCode;
import com.benben.wechat.mini.model.AssessCodeOrder;
import com.benben.wechat.mini.repository.AssessCodeOrderRepository;
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

    final static private Map<Integer, Integer> AMOUNT_TO_FEE =
            Map.of(
                    1, 1,
                    2, 2,
                    3, 3
            );

    final static private String ASSESS_CODE_WECHAT_ORDER_BODY =
            "本本教育:志愿填报系统:测评码";

    final static private int CODE_LENGTH = 8;
    final static private int MAX_CODE_GENERATE_RETRY = 3;

    final private AssessCodeOrderRepository assessCodeOrderRepository;
    final private AssessCodeRepository assessCodeRepository;

    @Autowired
    public AssessCodeService(
            AssessCodeOrderRepository assessCodeOrderRepository,
            AssessCodeRepository assessCodeRepository) {

        this.assessCodeOrderRepository = assessCodeOrderRepository;
        this.assessCodeRepository = assessCodeRepository;
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

    public static class InvalidAssessCodePurchaseAmount
            extends RuntimeException {
    }

    public static class FailToGenerateAssessCode
            extends RuntimeException {
    }
}
