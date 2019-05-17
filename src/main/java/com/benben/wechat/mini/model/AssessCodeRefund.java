package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class AssessCodeRefund {

    @Id
    private String id;
    private String orderId;
    private String orderItemId;
    private Integer fee;
    private State state;
    private String payNotify;

    public enum State {

        REFUNDING,
        REFUND_FAIL,
        REFUND_SUCCESS
    }
}
