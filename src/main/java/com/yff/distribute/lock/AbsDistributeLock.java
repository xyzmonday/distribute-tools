package com.yff.distribute.lock;

import com.yff.distribute.entity.DistributeLock;
import com.yff.distribute.lock.model.LockRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public abstract class AbsDistributeLock implements IDistributeLock {
    //默认最大重试次数
    private static final int DEFAULT_MAX_RETRY = 100;
    //默认重试延迟时间
    private static final long DEFAULT_RETRY_DELAY = 100L;
    //默认锁的key
    private static final String DEFAULT_LOCK_KEY = "default_lock";
    //默认重入锁的标识
    private static final String DEFAULT_LOCK_DESC = "default_lock_desc";
    //默认锁的过期时间 1分钟
    private static final long DEFAULT_EXPIRE = 60 * 1000L;


    /**
     * 校验请求参数
     *
     * @param lockRequest
     */
    protected void checkLockRequest(LockRequest lockRequest) {
        if (StringUtils.isEmpty(lockRequest.getKey())) {
            lockRequest.setKey(DEFAULT_LOCK_KEY);
        }
        if (StringUtils.isEmpty(lockRequest.getDesc())) {
            lockRequest.setDesc(DEFAULT_LOCK_DESC);
        }

        if (lockRequest.getRetryDelay() == null || lockRequest.getRetryDelay() <= 0) {
            lockRequest.setRetryDelay(DEFAULT_RETRY_DELAY);
        }

        if (lockRequest.getMaxRetryCount() == null || lockRequest.getMaxRetryCount() <= 0) {
            lockRequest.setMaxRetryCount(DEFAULT_MAX_RETRY);
        }
        if(lockRequest.getExpireTime() == null) {
            lockRequest.setExpireTime(DEFAULT_EXPIRE);
        }
    }

    /**
     * 初始化锁
     *
     * @param lockRequest
     * @return
     */
    protected DistributeLock initDistributeLock(LockRequest lockRequest) {
        checkLockRequest(lockRequest);
        DistributeLock distributeLock = new DistributeLock();
        lockRequest.setExpireTime(lockRequest.getExpireTime() == null ? 0L : lockRequest.getExpireTime());
        distributeLock.setLockName(lockRequest.getKey());
        distributeLock.setLockDesc(lockRequest.getDesc());
        distributeLock.setCreateTime(new Date());
        distributeLock.setCount(1);
        //默认锁的过期时间为0
        distributeLock.setExpireTime(0);
        distributeLock.setLockStatus((byte) 1);
        return distributeLock;
    }


}
