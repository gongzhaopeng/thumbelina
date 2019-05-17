package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class Assessment {

    @Id
    private String id;
    private Long createTime;
    private String subject;
    private String owner;
    private String assessCode;
    private List<Module> modules;

    @Data
    public static class Module {

        private String id;
        private List<Answer> answers;
    }

    @Data
    public static class Answer {

        private String questionId;
        private String content;
    }
}
