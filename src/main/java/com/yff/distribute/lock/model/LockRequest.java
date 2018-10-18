package com.yff.distribute.lock.model;


/**
 * 获取锁请求
 */
public class LockRequest {
    /**
     * 锁的key
     */
    private String key;

    /**
     * 锁的值或者描述，mysql实现重入锁的标识，
     * 在重入锁的情况下保存机器和线程的信息。
     */
    private String desc;

    /**
     * 锁的状态
     */
    private Integer status;

    /**
     * 加锁的时间（毫秒），超过这个时间后锁会自动释放
     */
    private Long expireTime;

    /**
     * 重试延时时间（毫秒）
     */
    private Long retryDelay;

    /**
     * 最大的重试次数
     */
    private Integer maxRetryCount;

    /**
     * zk分布式锁的路径
     */
    private String lockPath;

    public String getLockPath() {
        return lockPath;
    }

    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }
}
