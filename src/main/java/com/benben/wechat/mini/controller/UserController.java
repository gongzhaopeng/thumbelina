package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    public UserController() {

    }

    @GetMapping("/{openid}")
    public UserInfoResp getUserInfoByOpenid(
            @PathVariable("openid") String openid) {

        // TODO
        return null;
    }

    @PutMapping("/{openid}/profile/custom")
    public CustomProfileUpdateResp updateCustomProfile(
            @PathVariable("openid") String openid,
            @Valid @RequestBody User.CustomProfile customProfile) {

        // TODO

        return null;
    }

    @Getter
    @Setter
    static class UserInfoResp extends CommonResponse {

        private String openid;
        private User.CustomProfile customProfile;
        private List<User.AssessCode> assessCodes;
        private List<User.Assessment> assessments;
    }

    @Getter
    @Setter
    static class CustomProfileUpdateResp extends CommonResponse {
    }
}
