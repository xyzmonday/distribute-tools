package com.yff.distribute.queue;

import com.yff.distribute.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

/**
 * 基于redis的队列，实现原理基于leftPop和rightPush命令
 * http://annan211.iteye.com/blog/2338429
 * https://www.jianshu.com/u/5f4fd06b24dd
 * https://blog.csdn.net/jslcylcy/article/details/78201812
 */
@Component
public class RedisQueue {


    @Autowired
    private RedisService redisService;


    @Resource(name = "messageQueueThreadPool")
    private ExecutorService executorService;

    public void push(Message message) {
        redisService.lRightPush();
    }


}
