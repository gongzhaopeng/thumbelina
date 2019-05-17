package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class AssessCode {

    @Id
    private String code;
    private State state;
    private String owner;
    private String occupiedBy;
    private String assessmentId;
    private String orderId;
    private String orderItemId;
    private String refundId;

    // TODO

    public enum State {
        FRESH,
        OCCUPIED,
        REFUNDING,
        REFUNDED
    }
}
