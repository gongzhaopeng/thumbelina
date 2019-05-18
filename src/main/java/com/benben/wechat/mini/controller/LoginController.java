package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.WechatAuthCode2SessionInvoker;
import com.benben.wechat.mini.model.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/login")
public class LoginController {

    final private WechatAuthCode2SessionInvoker authCode2SessionInvoker;

    @Autowired
    public LoginController(
            WechatAuthCode2SessionInvoker authCode2SessionInvoker) {

        this.authCode2SessionInvoker = authCode2SessionInvoker;
    }

    /**
     * TODO Remember to modify the return-type.
     *
     * @return
     */
    @PostMapping("/wechat/jscode")
    public WechatJscodeLoginResp loginByWechatJscode(
            @Valid @RequestBody WechatJscodeLoginReq loginReq) {

        final var ret = authCode2SessionInvoker.invoke(loginReq.jscode);

        // TODO Remember to add business logic.

        return null;
    }

    @Data
    @Validated
    static class WechatJscodeLoginReq {

        @NotBlank
        private String jscode;

        @Valid
        @NotNull
        private WechatUserInfo userInfo;

        @Data
        @Validated
        static class WechatUserInfo {

            private String nickName;
            private String avatarUrl;
            private String gender;
            private String city;
            private String province;
            private String country;
            private String language;
        }
    }

    @Getter
    @Setter
    static class WechatJscodeLoginResp extends CommonResponse {

        private String openid;
        private String sessionKey;
        private User.CustomProfile customProfile;
        private List<User.AssessCode> assessCodes;
        private List<User.Assessment> assessments;
    }
}
