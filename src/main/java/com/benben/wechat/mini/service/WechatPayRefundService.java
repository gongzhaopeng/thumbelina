package com.benben.wechat.mini.service;

import com.benben.wechat.mini.apiinvoker.FatalExternalApiInvokeException;
import com.benben.wechat.mini.apiinvoker.WechatPayRefundInvoker;
import com.benben.wechat.mini.configuration.WechatPayConfiguration;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WechatPayRefundService {

    final private WechatPayRefundInvoker wechatPayRefundInvoker;
    final private WechatPayConfiguration wechatPayConfig;

    public WechatPayRefundService(
            WechatPayRefundInvoker wechatPayRefundInvoker,
            WechatPayConfiguration wechatPayConfig) {

        this.wechatPayRefundInvoker = wechatPayRefundInvoker;
        this.wechatPayConfig = wechatPayConfig;
    }

    /**
     * @param orderBusinessFields
     * @return
     * @throws WechatPayRefundInvoker.TradeOverdueException
     * @throws WechatPayRefundInvoker.NoEnoughBalanceException
     * @throws FatalExternalApiInvokeException
     */
    public Map<String, String> refund(
            Map<String, Object> orderBusinessFields) {

        final var reqFields = new HashMap<>(orderBusinessFields);

        return refundWithRetry(reqFields, 0);
    }

    private Map<String, String> refundWithRetry(
            Map<String, Object> reqFields,
            int retryCount) {

        try {
            return wechatPayRefundInvoker.invoke(reqFields);
        } catch (WechatPayRefundInvoker.NeedRetryException nre) {
            if (retryCount <= wechatPayConfig.getRefundRetryTimes()) {

                try {
                    TimeUnit.MILLISECONDS.sleep(wechatPayConfig.getRefundRetryDelay());
                } catch (InterruptedException ire) {
                    // This kind of Java again.
                }

                return refundWithRetry(reqFields, retryCount + 1);
            } else {
                throw new FatalExternalApiInvokeException(
                        String.format("The configured refund-retry-times is %d",
                                wechatPayConfig.getRefundRetryTimes()),
                        nre
                );
            }
        }
    }
}
