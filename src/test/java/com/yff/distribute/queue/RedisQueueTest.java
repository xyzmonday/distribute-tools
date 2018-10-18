package com.yff.distribute.queue;

import com.yff.distribute.DisToolsApplicationTest;
import com.yff.distribute.redis.RedisService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisQueueTest extends DisToolsApplicationTest {

    @Autowired
    private RedisService redisService;


    @Test
    public void sendMessage() {
        redisService.convertAndSend("name","yuanfengfan");
    }
}
