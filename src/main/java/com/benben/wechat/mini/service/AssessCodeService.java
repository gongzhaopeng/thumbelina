package com.benben.wechat.mini.service;

import com.benben.wechat.mini.apiinvoker.WechatPayUnifiedorderInvoker;
import com.benben.wechat.mini.model.AssessCodeOrder;
import com.benben.wechat.mini.repository.AssessCodeOrderRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    final private AssessCodeOrderRepository assessCodeOrderRepository;

    @Autowired
    public AssessCodeService(
            AssessCodeOrderRepository assessCodeOrderRepository) {

        this.assessCodeOrderRepository = assessCodeOrderRepository;
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

    public static class InvalidAssessCodePurchaseAmount
            extends RuntimeException {
    }
}
