package com.benben.wechat.mini.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

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
        private String state;
    }

    @Data
    public static class Assessment {

        private String id;
        private Long createTime;
        private String subject;
        private List<String> completedModules;
    }
}
