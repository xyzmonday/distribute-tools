package com.yff.distribute.lock.zk;

import com.yff.distribute.lock.AbsDistributeLock;
import com.yff.distribute.lock.IDistributeLock;
import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

@Service("zkDistributeLock")
public class ZkDistributeLock extends AbsDistributeLock implements IDistributeLock {
    final static Logger LOG = LoggerFactory.getLogger(ZkDistributeLock.class);

    /**
     * 锁的父目录
     */
    @Value("${zk.distributelock.dir}")
    private String dir;

    @Value("${zk.distributelock.host}")
    private String host;

    @Value("${zk.distributelock.timeout}")
    private int timeOut;

    /**
     * 锁的数据
     */
    private byte[] data = {0x12, 0x34};

    /**
     * zookeeper的链接
     */
    private ZooKeeper zooKeeper;

    private CountDownLatch connect = new CountDownLatch(1);

    @PostConstruct
    public void init() {
        try {
            zooKeeper = new ZooKeeper(host, timeOut, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.None) {
                        connect.countDown();
                    }
                }
            });
            connect.await();
            //确保父目录必须存在
            ensurePathExists(dir);
        } catch (IOException e) {
            zooKeeper = null;
            LOG.error("连接zookeeper出错:{}", e);
        } catch (InterruptedException e) {
            LOG.error("等待初始化zookeeper出错:{}", e);
        }
    }

    @Override
    public LockResponse acquire(final LockRequest lockRequest) throws InterruptedException, KeeperException {
        if (zooKeeper == null) {
            throw new ZkDistributeLockException("zookeeper还没有初始化完成!!!!");
        }
        checkLockRequest(lockRequest);
        final LockResponse response = new LockResponse();
        int retry = 0;
        int maxRetryCount = lockRequest.getMaxRetryCount();
        long expire = lockRequest.getExpireTime();
        long nowTime = System.currentTimeMillis();
        while (true) {
            if (++retry > maxRetryCount) {
                response.setSuccess(false);
                return response;
            }

            List<String> names = zooKeeper.getChildren(dir, false);//所有子节点的节点名
            //2. 判断当前路径是否存在
            String prefix = "x-" + zooKeeper.getSessionId() + "-" + Thread.currentThread().getId() + "-";
            String name = null;
            for (String path : names) {
                if (path.startsWith(prefix)) {
                    //说明该节点已经创建
                    name = dir.concat("/").concat(path);
                    break;
                }
            }
            if (name == null) {
                //说明该节点还没有创建过
                name = dir.concat("/").concat(prefix);
                name = zooKeeper.create(name, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }
            //3. 判断刚刚创建的节点是否已经删除
            names = zooKeeper.getChildren(dir, false);
            if(names.isEmpty()) {
                continue;
            }
            ZNodeName newNode = new ZNodeName(name);
            SortedSet<ZNodeName> sortedNodes = new TreeSet<ZNodeName>();
            for (String node : names) {
                sortedNodes.add(new ZNodeName(dir.concat("/").concat(node)));
            }
            //排在newNode前面的节点
            SortedSet<ZNodeName> lessThanMe = ((TreeSet<ZNodeName>) sortedNodes).headSet(newNode);
            //4. 判断当前节点是否是第一个节点
            if (lessThanMe.size() < 1) {
                LOG.info(name + "节点获取到锁");
                //说明是第一个节点
                response.setSuccess(true);
                response.setLockPath(name);
                return response;
            }

            //5. 如果还有比自己小的节点，那么监听这个节点的删除
            final CountDownLatch latch = new CountDownLatch(1);
            //注意这里有可能获取完lessThanMe集合里面最小的节点后，在监听之前，改节点被删除了
            String lastNode = lessThanMe.last().getName();
            Stat stat = null;
            try {
                stat = zooKeeper.exists(lastNode, new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
                            latch.countDown();
                        }
                    }
                });
            } catch (Exception e) {
                LOG.error("监听节点出错:" + e);
            }
            if (stat == null) {
                //如果已经被删除，那么需要重新监听。所有的情况都是走这个逻辑
                continue;
            }
            LOG.info(name + "正在等待:" + lastNode + "...");
            //如果已经监听了，那么等待监听的节点被删除通知，再次去竞争锁
            //如果使用await(retry * retryDelay, TimeUnit.MILLISECONDS) 那么这里自旋等待非常重要，有可能超时获取不到锁
            latch.await();
            long currentTime = System.currentTimeMillis();
            if((currentTime - nowTime) >= expire) {
                LOG.warn(name + "获取锁超时...");
                response.setSuccess(false);
                return response;
            }
        }
    }

    @Override
    public void release(LockRequest lockRequest) throws Exception {
        if (zooKeeper == null) {
            throw new ZkDistributeLockException("zookeeper还没有初始化完成!!!!");
        }
        LOG.info(lockRequest.getLockPath() + "释放锁");
        zooKeeper.delete(lockRequest.getLockPath(), -1);
    }


    /**
     * 保证path路径下存在节点
     *
     * @param path
     */
    private void ensurePathExists(String path) {
        while (true) {
            try {
                Stat stat = zooKeeper.exists(path, false);
                if (stat != null) {
                    return;
                }
                zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                return;
            } catch (KeeperException e) {
                e.printStackTrace();
                continue;
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
