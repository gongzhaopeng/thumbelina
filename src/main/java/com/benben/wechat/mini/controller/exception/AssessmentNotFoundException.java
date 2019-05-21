package com.benben.wechat.mini.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.NOT_FOUND,
        reason = "Assessment Not Found")
public class AssessmentNotFoundException extends RuntimeException {
}
