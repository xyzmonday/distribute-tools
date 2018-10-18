package com.yff.distribute.lock;

import com.yff.distribute.DisToolsApplication;
import com.yff.distribute.DisToolsApplicationTest;
import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

/**
 * zk秒杀测试
 * 耗时 19s,库存 0
 */
public class SeckillTest extends DisToolsApplicationTest {

    int threadCount = 100;
    int count = threadCount;

    @Resource(name = "redisDistributeLock")
    IDistributeLock distributeLock;

    @Test
    public void testSecKill() {

        CountDownLatch endCount = new CountDownLatch(threadCount);
        CountDownLatch beginCount = new CountDownLatch(1);

        Thread[] threads = new Thread[threadCount];
        //起500个线程，秒杀第一个商品
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        //等待在一个信号量上，挂起
                        beginCount.await();
                        LockRequest lockRequest = new LockRequest();
                        lockRequest.setKey("disLock");
                        lockRequest.setDesc("disLock" + Thread.currentThread().getName());
                        lockRequest.setExpireTime(600L);
                        lockRequest.setMaxRetryCount(100);
                        lockRequest.setRetryDelay(100L);
                        LockResponse acquire = distributeLock.acquire(lockRequest);
                        if (acquire.isSuccess()) {
                            count--;
                            lockRequest.setLockPath(acquire.getLockPath());
                            distributeLock.release(lockRequest);
                        }
                        endCount.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[i].start();

        }

        long startTime = System.currentTimeMillis();
        //主线程释放开始信号量，并等待结束信号量，这样做保证1000个线程做到完全同时执行，保证测试的正确性
        beginCount.countDown();
        try {
            //主线程等待结束信号量
            endCount.await();
            //观察秒杀结果是否正确
            System.out.println("秒杀结束，最后库存" + count);
            System.out.println("total cost " + (System.currentTimeMillis() - startTime) / 1000 + "s");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
