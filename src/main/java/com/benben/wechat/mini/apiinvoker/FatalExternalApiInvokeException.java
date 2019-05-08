package com.benben.wechat.mini.apiinvoker;

class FatalExternalApiInvokeException
        extends RuntimeException {

    FatalExternalApiInvokeException(String message) {

        super(message);
    }

    FatalExternalApiInvokeException(String message, Throwable cause) {

        super(message, cause);
    }
}
