package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class AssessCodeOrder {

    @Id
    private String id;
    private String owner;
    private List<Item> items;
    private Integer fee;
    private State state;
    private String payNotify;
    // TODO

    @Data
    public static class Item {

        private String id;
        private String refundId;
        private AssessCodeRefund.State refundState;
    }

    public enum State {

        UNPAID,
        PAID
    }
}
