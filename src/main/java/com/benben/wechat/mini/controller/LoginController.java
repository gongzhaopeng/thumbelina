package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.WechatAuthCode2SessionInvoker;
import com.benben.wechat.mini.model.User;
import com.benben.wechat.mini.repository.UserRepository;
import com.benben.wechat.mini.service.UserUpdateLockService;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/login")
public class LoginController {

    final private WechatAuthCode2SessionInvoker authCode2SessionInvoker;
    final private UserUpdateLockService userUpdateLockService;
    final private UserRepository userRepository;

    @Autowired
    public LoginController(
            WechatAuthCode2SessionInvoker authCode2SessionInvoker,
            UserUpdateLockService userUpdateLockService,
            UserRepository userRepository) {

        this.authCode2SessionInvoker = authCode2SessionInvoker;
        this.userUpdateLockService = userUpdateLockService;
        this.userRepository = userRepository;
    }

    @PostMapping("/wechat/jscode")
    public WechatJscodeLoginResp loginByWechatJscode(
            @Valid @RequestBody WechatJscodeLoginReq loginReq) {

        final var wechatInvokeRet =
                authCode2SessionInvoker.invoke(loginReq.jscode);
        final var openid = wechatInvokeRet.getOpenid();

        final var loginedUser = userUpdateLockService.doWithLock(openid, () -> {

            var user = userRepository.findById(openid)
                    .map(u -> {
                        updateWechatInfoOfUser(u, loginReq, wechatInvokeRet);
                        return u;
                    })
                    .orElse(constructNewUser(loginReq, wechatInvokeRet));

            return userRepository.save(user);
        });


        return WechatJscodeLoginResp.of(loginedUser);
    }

    private User constructNewUser(
            WechatJscodeLoginReq loginReq,
            WechatAuthCode2SessionInvoker.Return wechatInvokeRet) {

        final var newUser = new User();
        newUser.setOpenid(wechatInvokeRet.getOpenid());
        newUser.setCreateTime(System.currentTimeMillis());

        updateWechatInfoOfUser(newUser, loginReq, wechatInvokeRet);

        return newUser;
    }

    private void updateWechatInfoOfUser(
            User user,
            WechatJscodeLoginReq loginReq,
            WechatAuthCode2SessionInvoker.Return wechatInvokeRet) {

        final var wechat = new User.WechatInfo();
        wechat.setNickName(loginReq.userInfo.getNickName());
        wechat.setAvatarUrl(loginReq.userInfo.getAvatarUrl());
        wechat.setGender(loginReq.userInfo.getGender());
        wechat.setCity(loginReq.userInfo.getCity());
        wechat.setProvince(loginReq.userInfo.getProvince());
        wechat.setCountry(loginReq.userInfo.getCountry());
        wechat.setLanguage(loginReq.userInfo.getLanguage());

        final var wechatLogin = new User.WechatLoginInfo();
        wechatLogin.setSessionKey(wechatInvokeRet.getSessionKey());
        wechatLogin.setUnionid(wechatInvokeRet.getUnionid());
        wechatLogin.setLoginTime(System.currentTimeMillis());

        wechat.setLogin(wechatLogin);

        user.setWechat(wechat);
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

        static WechatJscodeLoginResp of(User user) {

            final var resp = new WechatJscodeLoginResp();
            resp.setOpenid(user.getOpenid());
            resp.setSessionKey(user.getWechat().getLogin().getSessionKey());
            resp.setCustomProfile(user.getCustomProfile());
            resp.setAssessCodes(user.getAssessCodes());
            resp.setAssessments(user.getAssessments());

            return resp;
        }

        private String openid;
        private String sessionKey;
        private User.CustomProfile customProfile;
        private List<User.AssessCode> assessCodes;
        private List<User.Assessment> assessments;
    }
}
