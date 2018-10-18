package com.yff.distribute.lock.mysql;

import com.yff.distribute.entity.DistributeLock;
import com.yff.distribute.lock.AbsDistributeLock;
import com.yff.distribute.lock.IDistributeLock;
import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;
import com.yff.distribute.mapper.DistributeLockMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * mysql实现分布式锁（insert）.
 */
@Service(value = "mysqlDisLock")
public class MysqlDisLock extends AbsDistributeLock implements IDistributeLock {

    private final static Logger LOG = LoggerFactory.getLogger(MysqlDisLock.class);

    @Autowired
    DistributeLockMapper distributeLockMapper;

    @Override
    public LockResponse acquire(LockRequest lockRequest) throws Exception {
        checkLockRequest(lockRequest);
        LockResponse response = new LockResponse();
        int retry = 0;
        //初始化锁
        DistributeLock distributeLock = initDistributeLock(lockRequest);
        long nowTime = System.currentTimeMillis();
        Long retryDelay = lockRequest.getRetryDelay();
        while (true) {
            try {
                if (++retry > lockRequest.getMaxRetryCount()) {
                    response.setSuccess(false);
                    return response;
                }
                long currentTime = System.currentTimeMillis();
                //先查询该机器的线程是否已经获取了锁
                DistributeLock lock = distributeLockMapper.findOne(lockRequest.getKey(), lockRequest.getDesc());
                if (lock != null && lock.getCount() >= 1 && (currentTime - nowTime) <= lock.getExpireTime()) {
                    distributeLockMapper.acquireReentryLock(lock);
                    response.setSuccess(true);
                    return response;
                }
                //设置锁的过期时间
                distributeLock.setExpireTime(lockRequest.getExpireTime());
                distributeLockMapper.insertRecord(distributeLock);
                response.setSuccess(true);
                return response;
            } catch (Exception e1) {
                //获取锁失败，自旋
                try {
                    Thread.sleep(retry * retryDelay);
                } catch (InterruptedException e2) {
                    LOG.error("等待获取锁时被中断...");
                    response.setSuccess(false);
                    return response;
                }

            }
        }
    }

    @Override
    public void release(LockRequest lockRequest) throws Exception {
        int retry = 1;
        while (true) {
            try {
                //这里如果重入，那么瞬间的多个删除请求过来，发生死锁
                Integer rows = distributeLockMapper.releaseReentryLock(lockRequest.getKey(), lockRequest.getDesc());
                if (rows > 0) {
                    //释放重入锁成功
                    DistributeLock lock = distributeLockMapper.findOne(lockRequest.getKey(), lockRequest.getDesc());
                    //如果需要该重入锁已经完毕
                    if (lock != null && lock.getCount() <= 0) {
                        distributeLockMapper.deleteRecord(lockRequest.getKey());
                        return;
                    }
                } else {
                    distributeLockMapper.deleteRecord(lockRequest.getKey());
                    return;
                }

            } catch (Exception e) {
                Thread.sleep(retry * lockRequest.getRetryDelay());
                retry++;
                if (retry <= lockRequest.getMaxRetryCount()) {
                    continue;
                }
                break;
            }
        }
    }
}
