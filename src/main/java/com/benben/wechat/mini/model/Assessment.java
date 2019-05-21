package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("Assessment")
@Data
public class Assessment {

    @Id
    private String id;
    private Long createTime;
    private String subject;
    private String owner;
    private String assessCode;
    private List<Module> modules;

    public Module forceAcquireModule(String moduleId) {

        if (modules == null) {
            modules = new ArrayList<>();
        }

        return modules.stream()
                .filter(module -> module.getId().equals(moduleId))
                .findFirst()
                .orElseGet(() -> {
                    final var newModule = new Module();
                    newModule.setId(moduleId);

                    return newModule;
                });
    }

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
