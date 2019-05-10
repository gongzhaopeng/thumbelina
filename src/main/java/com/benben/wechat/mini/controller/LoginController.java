package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.apiinvoker.WechatAuthCode2SessionInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/login")
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
     * @param jscode
     * @return
     */
    @PostMapping("/wechat/jscode/{jscode}")
    public String loginByWechatJscode(
            @PathVariable("jscode") String jscode) {

        final var ret = authCode2SessionInvoker.invoke(jscode);

        // TODO Remember to add business logic.

        return ret.getOpenid();
    }
}
