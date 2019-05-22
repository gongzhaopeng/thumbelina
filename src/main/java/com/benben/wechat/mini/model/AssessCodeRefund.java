package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("AssessCodeRefund")
@Data
public class AssessCodeRefund {

    @Id
    private String id;
    private Long createTime;
    private String orderId;
    private String orderItemId;
    private Integer fee;
    private State state;
    private String wepayNotify;
    private Long wepayNotifyTs;
    private String wepayRid;

    public enum State {

        REFUNDING,
        REFUND_FAIL,
        REFUND_SUCCESS
    }
}
