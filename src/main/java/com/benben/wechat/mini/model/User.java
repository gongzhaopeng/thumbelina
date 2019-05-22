package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document("User")
@Data
public class User {

    @Id
    private String openid;
    private Long createTime;
    private WechatInfo wechat;
    private CustomProfile customProfile;
    private List<AssessCode> assessCodes;
    private List<Assessment> assessments;

    public Optional<AssessCode> getAssessCode(String code) {

        return Optional.ofNullable(assessCodes).flatMap(acs ->
                acs.stream()
                        .filter(ac -> code.equals(ac.getCode()))
                        .findFirst()
        );
    }

    public void addAssessCode(AssessCode assessCode) {

        if (assessCodes == null) {
            assessCodes = new ArrayList<>();
        }

        assessCodes.add(assessCode);
    }

    public Optional<Assessment> getAssessment(String assessmentId) {

        return Optional.ofNullable(assessments).flatMap(as ->
                as.stream()
                        .filter(a -> assessmentId.equals(a.getId()))
                        .findFirst()
        );
    }

    public void addAssessment(Assessment assessment) {

        if (assessments == null) {
            assessments = new ArrayList<>();
        }

        assessments.add(assessment);
    }

    @Data
    public static class WechatInfo {

        private WechatLoginInfo login;
        private String nickName;
        private String avatarUrl;
        private String gender;
        private String city;
        private String province;
        private String country;
        private String language;
    }

    @Data
    public static class WechatLoginInfo {

        private String sessionKey;
        private String unionid;
        private Long loginTime;
    }

    @Data
    public static class CustomProfile {

        private String gender;
        /**
         * 文,理...分科
         */
        private String subject;
        /**
         * 语文,数学,英语,化学...
         */
        private List<String> favorCourses;
        /**
         * 偏爱的专业
         */
        private List<String> favorSpecs;
        private List<String> location;
        private String school;
    }

    @Data
    public static class AssessCode {

        private String code;
        private Long createTime;
        private com.benben.wechat.mini.model.AssessCode.State state;
    }

    @Data
    public static class Assessment {

        private String id;
        private Long createTime;
        private String subject;
        private Set<String> completedModules;

        public void addCompletedModule(String moduleId) {

            if (completedModules == null) {
                completedModules = new HashSet<>();
            }

            completedModules.add(moduleId);
        }
    }
}
