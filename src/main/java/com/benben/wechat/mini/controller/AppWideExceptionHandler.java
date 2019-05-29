package com.benben.wechat.mini.controller;

import com.benben.wechat.mini.service.UserUpdateLockService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppWideExceptionHandler {

    @ExceptionHandler(UserUpdateLockService.FailToAcquireUserUpdateLock.class)
    public CommonResponse failToAcquireUserUpdateLockHandler() {

        final var resp = new CommonResponse();
        resp.setStatusCode(CommonResponse.SC_SERVICE_BUSY);
        resp.setStatusDetail("Target service is busy, retry later.");

        return resp;
    }
}
