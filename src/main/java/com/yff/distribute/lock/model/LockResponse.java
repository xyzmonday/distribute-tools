package com.yff.distribute.lock.model;

public class LockResponse {
    private boolean isSuccess;
    private String lockPath;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getLockPath() {
        return lockPath;
    }

    public void setLockPath(String lockPath) {
        this.lockPath = lockPath;
    }
}
