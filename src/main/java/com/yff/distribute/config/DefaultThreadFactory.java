package com.yff.distribute.config;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
    private AtomicInteger number;
    private ThreadGroup threadGroup;

    public DefaultThreadFactory(String namePrefix) {
        this.number = new AtomicInteger(1);
        ThreadGroup root = new ThreadGroup(namePrefix);
        this.threadGroup = new ThreadGroup(root, namePrefix + "-pool");
    }

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(this.threadGroup, r);
        thread.setName(this.threadGroup.getName() + "-" + this.number.getAndIncrement());
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }

        if (5 != thread.getPriority()) {
            thread.setPriority(5);
        }

        return thread;
    }
}