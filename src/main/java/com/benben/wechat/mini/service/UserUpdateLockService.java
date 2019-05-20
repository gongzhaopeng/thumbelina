package com.benben.wechat.mini.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

@Service
public class UserUpdateLockService {

    static final private Duration LOCK_TIMEOUT =
            Duration.ofSeconds(30);

    static private String lockKey(String openid) {
        return String.format(
                "intent:lock:user:update:%s", openid);
    }

    private StringRedisTemplate redisTemplate;

    @Autowired
    public UserUpdateLockService(
            StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    /**
     * @param openid
     * @param lockRetryTimes
     * @param task
     * @param <T>
     * @return
     * @throws FailToAcquireLock
     */
    public <T> T doWithLock(
            String openid, int lockRetryTimes, Callable<T> task) {

        final var lockKey = lockKey(openid);

        if (!acquireLock(lockKey, lockRetryTimes)) {

            throw new FailToAcquireLock();
        }

        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            releaseLock(lockKey);
        }
    }

    /**
     * @param openid
     * @param task
     * @param <T>
     * @return
     * @throws FailToAcquireLock
     */
    public <T> T doWithLock(
            String openid, Callable<T> task) {

        return doWithLock(openid, 0, task);
    }

    private boolean acquireLock(String lockKey, int lockRetryTimes) {

        return IntStream.range(0, lockRetryTimes + 1).filter(i ->
                Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(lockKey,
                        String.valueOf(System.currentTimeMillis()), LOCK_TIMEOUT))
                        .orElseThrow()).findFirst().isPresent();
    }

    private void releaseLock(String lockKey) {

        redisTemplate.delete(lockKey);
    }

    static public class FailToAcquireLock
            extends RuntimeException {
    }
}
