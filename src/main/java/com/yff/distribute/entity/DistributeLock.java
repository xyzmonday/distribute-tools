package com.yff.distribute.entity;


import java.util.Date;

/**
 * mysql锁的model
 */
public class DistributeLock {
    private Integer id;
    private String lockName;
    private String lockDesc;
    private byte lockStatus;
    private long expireTime;
    private Date createTime;
    private Integer count;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public String getLockDesc() {
        return lockDesc;
    }

    public void setLockDesc(String lockDesc) {
        this.lockDesc = lockDesc;
    }

    public byte getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(byte lockStatus) {
        this.lockStatus = lockStatus;
    }


    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
