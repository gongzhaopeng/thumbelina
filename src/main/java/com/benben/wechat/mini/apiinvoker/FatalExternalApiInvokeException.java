package com.benben.wechat.mini.apiinvoker;

public class FatalExternalApiInvokeException
        extends RuntimeException {

    public FatalExternalApiInvokeException(String message) {

        super(message);
    }

    public FatalExternalApiInvokeException(String message, Throwable cause) {

        super(message, cause);
    }
}
