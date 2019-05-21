package com.benben.wechat.mini.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
public class AssessCodeCollLockService {

    static final private Duration LOCK_TIMEOUT =
            Duration.ofSeconds(30);
    static final private Duration LOCK_RETRY_DELAY =
            Duration.ofMillis(50);

    static final private String LOCK_KEY =
            "intent:lock:assesscode:coll";

    private StringRedisTemplate redisTemplate;

    public AssessCodeCollLockService(
            StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    /**
     * @param lockRetryTimes
     * @param task
     * @param <T>
     * @return
     * @throws FailToAcquireAssessCodeCollLock
     */
    public <T> T doWithLock(int lockRetryTimes, Callable<T> task) {

        if (!acquireLock(lockRetryTimes)) {

            throw new FailToAcquireAssessCodeCollLock();
        }

        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseLock();
        }
    }

    /**
     * @param task
     * @param <T>
     * @return
     * @throws FailToAcquireAssessCodeCollLock
     */
    public <T> T doWithLock(Callable<T> task) {

        return doWithLock(0, task);
    }

    private boolean acquireLock(int lockRetryTimes) {

        for (int i = 0; i < lockRetryTimes + 1; i++) {

            final var success = Optional.ofNullable(
                    redisTemplate.opsForValue().setIfAbsent(LOCK_KEY,
                            String.valueOf(System.currentTimeMillis()), LOCK_TIMEOUT))
                    .orElse(false);

            if (success) {
                return true;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(LOCK_RETRY_DELAY.toMillis());
            } catch (Exception e) {
                // This kind of Java again.
            }
        }

        return false;
    }

    private void releaseLock() {

        redisTemplate.delete(LOCK_KEY);
    }

    static public class FailToAcquireAssessCodeCollLock
            extends RuntimeException {
    }
}
