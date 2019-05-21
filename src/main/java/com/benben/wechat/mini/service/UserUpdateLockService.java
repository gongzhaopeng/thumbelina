package com.benben.wechat.mini.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
public class UserUpdateLockService {

    static final private Duration LOCK_TIMEOUT =
            Duration.ofSeconds(30);
    static final private Duration LOCK_RETRY_DELAY =
            Duration.ofMillis(50);

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
     * @throws FailToAcquireUserUpdateLock
     */
    public <T> T doWithLock(
            String openid, int lockRetryTimes, Callable<T> task) {

        final var lockKey = lockKey(openid);

        if (!acquireLock(lockKey, lockRetryTimes)) {

            throw new FailToAcquireUserUpdateLock();
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
     * @throws FailToAcquireUserUpdateLock
     */
    public <T> T doWithLock(
            String openid, Callable<T> task) {

        return doWithLock(openid, 0, task);
    }

    private boolean acquireLock(String lockKey, int lockRetryTimes) {

        for (int i = 0; i < lockRetryTimes + 1; i++) {

            final var success = Optional.ofNullable(
                    redisTemplate.opsForValue().setIfAbsent(lockKey,
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

    private void releaseLock(String lockKey) {

        redisTemplate.delete(lockKey);
    }

    static public class FailToAcquireUserUpdateLock
            extends RuntimeException {
    }
}
