package com.yff.distribute.lock;

import com.yff.distribute.DisToolsApplication;
import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;
import org.junit.Test;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * mysql分布式锁测试：
 * 耗时22s,count=100
 * redis分布式锁测试：
 *   非lua:耗时1s,count=100
 *   lua:基本不耗时，count=100
 * zk分布式锁测试：
 *   耗时：6s,count=100
 */
public class DisLockTest extends DisToolsApplication {
    //最大线程数
    private static final int MAX_THREAD_NUM = 500;
    //分布式环境中的机器信息
    private static final AtomicInteger MACHINE_NUM = new AtomicInteger(0);
    //计数器
    private int count = 0;

    @Resource(name = "redisDistributeLock")
    IDistributeLock distributeLock;

    /**
     * 测试通过22s
     * @throws Exception
     */
    @Test
    public void lockTest() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        Long startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_THREAD_NUM; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    doTask();
                }
            });
        }
        executor.shutdown();
        while (true) {
            if (executor.isTerminated()) {
                Long endTime = System.currentTimeMillis();
                Long time = (endTime - startTime) / 1000;
                System.out.println("任务执行完毕，耗时:" + time + "s");
                return;
            }
        }
    }

    /**
     * 测试mysql的重入锁
     * 22s
     */
    @Test
    public void reentryLockTest() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Long startTime = System.currentTimeMillis();
        for (int i = 1; i <= MAX_THREAD_NUM; i++) {
            //每5个线程为一组，作为重入
            executor.submit(new ReentryTask(i % 5));
        }
        executor.shutdown();
        while (true) {
            if (executor.isTerminated()) {
                Long endTime = System.currentTimeMillis();
                Long time = (endTime - startTime) / 1000;
                System.out.println("任务执行完毕，耗时:" + time + "s");
                return;
            }
        }
    }

    private void doTask() {
        LockRequest lockRequest = new LockRequest();
        lockRequest.setKey("disLock");
        lockRequest.setDesc(MACHINE_NUM.addAndGet(1) + "disLock" + Thread.currentThread().getName());
        lockRequest.setExpireTime(6000L);
        lockRequest.setMaxRetryCount(100);
        lockRequest.setRetryDelay(100L);
        try {
            LockResponse acquire = distributeLock.acquire(lockRequest);
            if (acquire.isSuccess()) {
                count++;
                System.out.println("count = " + count);
                lockRequest.setLockPath(acquire.getLockPath());
                distributeLock.release(lockRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class ReentryTask implements Runnable {
        private int num;

        public ReentryTask(int num) {
            this.num = num;
        }

        @Override
        public void run() {
            try {
                LockRequest lockRequest = new LockRequest();
                lockRequest.setKey("disLock");
                lockRequest.setDesc("disLock-" + num);
                //1分钟
                Long current = 10 * 60 * 1000L;
                lockRequest.setExpireTime(current);
                distributeLock.acquire(lockRequest);
                count++;
                distributeLock.acquire(lockRequest);
                count++;
                System.out.println("count = " + count);
                distributeLock.release(lockRequest);
                distributeLock.release(lockRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
