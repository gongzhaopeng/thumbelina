package com.benben.wechat.mini.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

@Service
public class AssessCodeCollLockService {

    static final private Duration LOCK_TIMEOUT =
            Duration.ofSeconds(30);

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

        return IntStream.range(0, lockRetryTimes + 1).filter(i ->
                Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(LOCK_KEY,
                        String.valueOf(System.currentTimeMillis()), LOCK_TIMEOUT))
                        .orElseThrow()).findFirst().isPresent();
    }

    private void releaseLock() {

        redisTemplate.delete(LOCK_KEY);
    }

    static public class FailToAcquireAssessCodeCollLock
            extends RuntimeException {
    }
}
