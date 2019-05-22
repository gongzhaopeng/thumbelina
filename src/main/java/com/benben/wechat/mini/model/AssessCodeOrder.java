package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("AssessCodeOrder")
@Data
public class AssessCodeOrder {

    @Id
    private String id;
    private Long createTime;
    private String owner;
    private List<Item> items;
    private Integer fee;
    private State state;
    private String wepayNotify;
    private Long wepayNotifyTs;
    private String wepayTid;

    @Data
    public static class Item {

        private String id;
        private String assessCode;
        private String refundId;
        private AssessCodeRefund.State refundState;
    }

    public enum State {

        UNPAID,
        PAID
    }
}
