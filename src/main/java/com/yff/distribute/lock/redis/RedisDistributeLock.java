package com.yff.distribute.lock.redis;

import com.yff.distribute.lock.AbsDistributeLock;
import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;
import com.yff.distribute.redis.RedisService;
import com.yff.distribute.utils.DistributeLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * redis实现分布式锁，支持阻塞，但是不支持重入
 */
@Service("redisDistributeLock")
public class RedisDistributeLock extends AbsDistributeLock {

    private final static Logger LOG = LoggerFactory.getLogger(RedisDistributeLock.class);

    @Autowired
    RedisService redisService;

    @Override
    public LockResponse acquire(LockRequest lockRequest) throws Exception {
        checkLockRequest(lockRequest);
        LockResponse response = new LockResponse();
        int retry = 0;
        int maxRetryCount = lockRequest.getMaxRetryCount();
        Long retryDelay = lockRequest.getRetryDelay();
        while (true) {
            if (++retry > maxRetryCount) {
                LOG.error("获取锁超时...");
                // 获取锁超时
                response.setSuccess(false);
                return response;
            }
            long nowTime = System.currentTimeMillis();
            // 容忍不同服务器时间有1秒内的误差
            long expireTime = nowTime + lockRequest.getExpireTime() + 1000;
            String key = lockRequest.getKey();
            if (redisService.setNX(key, DistributeLockUtil.longToBytes(expireTime))) {
                //这里setNX和expire不是原子性的，所以为了防止获取到锁的服务器或者进程kill导致的死锁，需要将setNX设置的时间get出来，进行比较
                redisService.expire(key, expireTime);
                response.setSuccess(true);
                return response;
            } else {
                //这里通过get和getset两次值进行比较，如果两次获取到的时间一致，那么认为获取锁。但是getset返回null值同样会出现死锁
                //别的客户端的超时时间
                byte[] oldExpireTime = (byte[]) redisService.get(key);
                if (oldExpireTime != null && DistributeLockUtil.bytesToLong(oldExpireTime) < nowTime) {
                    // 这个锁已经过期了，可以获得它
                    // PS: 如果setNX和expire之间客户端发生崩溃，可能会出现这样的情况
                    byte[] oldExpireTime2 = (byte[]) redisService.getset(key, DistributeLockUtil.longToBytes(expireTime));
                    if (oldExpireTime2 == null) {
                        //如果出现了这种情况，那么说明在get和getset之间还有del操作，根据分析此时应该获取到锁
                        redisService.expire(key, expireTime);
                        response.setSuccess(true);
                        return response;

                    } else if (Arrays.equals(oldExpireTime, oldExpireTime2)) {
                        //这里可以忽略掉getset的耗时，即获得了锁
                        redisService.expire(key, expireTime);
                        response.setSuccess(true);
                        return response;
                    } else {
                        // 被别人抢占了锁，setNX重试
                        try {
                            //这里sleep是为了减少对redis的压力
                            Thread.sleep(retry * retryDelay);
                        } catch (InterruptedException e) {
                            LOG.error("等待获取锁被中断....");
                            response.setSuccess(false);
                            return response;
                        }
                    }
                } else {
                    //表示锁已经被删除了或者还未超时，setNX重试
                    try {
                        //这里sleep是为了减少对redis的压力
                        Thread.sleep(retry * retryDelay);
                    } catch (InterruptedException e) {
                        LOG.error("等待获取锁被中断....");
                        response.setSuccess(false);
                        return response;
                    }
                }
            }
        }

    }

    @Override
    public void release(LockRequest lockRequest) throws Exception {
        redisService.del(lockRequest.getKey());
    }

}
