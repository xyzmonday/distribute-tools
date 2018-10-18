package com.yff.distribute.lock.redis;

import com.yff.distribute.lock.AbsDistributeLock;
import com.yff.distribute.lock.model.LockRequest;
import com.yff.distribute.lock.model.LockResponse;
import com.yff.distribute.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * 使用lua实现分布式锁.
 * if (redis.call('setnx', KEYS[1], ARGV[1]) == 1) then
 * redis.call('expire', KEYS[1], tonumber(ARGV[2]))
 * return true
 * else
 * return false
 * end
 */
@Service("redisLuaDistributeLock")
public class RedisLuaDistributeLock extends AbsDistributeLock {

    @Autowired
    RedisService redisService;

    private static final RedisScript<Boolean> SETNX_AND_EXPIRE_SCRIPT;

    private static final RedisScript<Boolean> DEL_IF_GET_EQUALS;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if (redis.call('setnx', KEYS[1], ARGV[1]) == 1) then\n");
        sb.append("\tredis.call('expire', KEYS[1], tonumber(ARGV[2]))\n");
        sb.append("\treturn true\n");
        sb.append("else\n");
        sb.append("\treturn false\n");
        sb.append("end");
        SETNX_AND_EXPIRE_SCRIPT = new RedisScriptImpl<Boolean>(sb.toString(), Boolean.class);
    }

    //如果获取到的锁时该机器的锁才能释放
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if (redis.call('get', KEYS[1]) == ARGV[1]) then\n");
        sb.append("\tredis.call('del', KEYS[1])\n");
        sb.append("\treturn true\n");
        sb.append("else\n");
        sb.append("\treturn false\n");
        sb.append("end");
        DEL_IF_GET_EQUALS = new RedisScriptImpl<Boolean>(sb.toString(), Boolean.class);
    }

    @Override
    public LockResponse acquire(LockRequest lockRequest) throws Exception {
        checkLockRequest(lockRequest);
        LockResponse response = new LockResponse();
        String value = UUID.randomUUID().toString() + "." + System.currentTimeMillis();
        lockRequest.setDesc(value);
        int retry = 0;
        int maxRetryCount = lockRequest.getMaxRetryCount();
        Long retryDelay = lockRequest.getRetryDelay();
        while (true) {
            if (++retry > maxRetryCount) {
                // 获取锁超时
                response.setSuccess(false);
                return response;
            }
            try {
                if (redisService.execute(SETNX_AND_EXPIRE_SCRIPT, Collections.singletonList(lockRequest.getKey()), value,
                        String.valueOf(lockRequest.getExpireTime()))) {
                    response.setSuccess(true);
                    return response;
                }
            } catch (Exception e) {
                response.setSuccess(false);
                return response;
            }
            try {
                //这里sleep是为了减少对redis的压力
                Thread.sleep(retry * retryDelay);
            } catch (InterruptedException e) {
                response.setSuccess(false);
                return response;
            }
        }
    }

    @Override
    public void release(LockRequest lockRequest) throws Exception {
        redisService.execute(DEL_IF_GET_EQUALS, Collections.singletonList(lockRequest.getKey()), lockRequest.getDesc());
    }


    private static class RedisScriptImpl<T> implements RedisScript<T> {
        private final String script;
        private final String sha1;
        private final Class<T> resultType;

        public RedisScriptImpl(String script, Class<T> resultType) {
            this.script = script;
            this.sha1 = DigestUtils.sha1DigestAsHex(script);
            this.resultType = resultType;
        }

        @Override
        public String getSha1() {
            return sha1;
        }

        @Override
        public Class<T> getResultType() {
            return resultType;
        }

        @Override
        public String getScriptAsString() {
            return script;
        }
    }
}
