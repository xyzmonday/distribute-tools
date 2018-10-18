package com.yff.distribute.lock;


import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;

public interface IDistributeLock {

    /**
     * 获取分布式锁
     * @param lockRequest
     * @return
     */
    LockResponse acquire(LockRequest lockRequest) throws Exception;

    /**
     * 释放分布式锁
     * @param lockRequest
     * @return
     */
    void release(LockRequest lockRequest) throws Exception;

}
