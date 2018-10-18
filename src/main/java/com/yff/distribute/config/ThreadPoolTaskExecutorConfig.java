package com.yff.distribute.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolTaskExecutorConfig {

    @Bean(name = "messageQueueThreadPool")
    public ExecutorService messageQueueThreadPool() {
        return new ThreadPoolExecutor(2, 10,
                60L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(2),
                new DefaultThreadFactory("redis-queue"));
    }

}
