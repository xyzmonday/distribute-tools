package com.yff.distribute.queue;

import java.io.Serializable;

/**
 * 消息定义
 */
public class Message implements Serializable {
    /**
     * 消息id
     */
    private String id;
    /**
     * 消息内容
     */
    private String content;

    /**
     * 重试次数
     */
    private int retryTimes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
