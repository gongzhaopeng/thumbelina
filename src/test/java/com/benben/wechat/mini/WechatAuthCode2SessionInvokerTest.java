package com.benben.wechat.mini;

import com.benben.wechat.mini.apiinvoker.WechatAuthCode2SessionInvoker;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WechatAuthCode2SessionInvokerTest {

    @Autowired
    WechatAuthCode2SessionInvoker wechatAuthCode2SessionInvoker;

    @Ignore
    @Test
    public void invokeWithFakedParams() {

        final var FACKED_JS_CODE = "FACKED_JS_CODE";

        wechatAuthCode2SessionInvoker.invoke(FACKED_JS_CODE);

        // TODO
    }
}
