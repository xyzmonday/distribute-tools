package com.yff.distribute.redis;

import com.yff.distribute.DisToolsApplicationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisServiceTest extends DisToolsApplicationTest {


    @Autowired
    private RedisService redisService;


    @Test
    public void test() {
        redisService.set("hello","world");
    }
}
