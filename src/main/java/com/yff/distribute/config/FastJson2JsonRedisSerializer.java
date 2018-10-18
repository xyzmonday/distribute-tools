
package com.yff.distribute.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;

public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Class<T> clazz;

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) {
        return t == null ? new byte[0] : JSON.toJSONString(t, new SerializerFeature[]{SerializerFeature.WriteClassName}).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            String args = new String(bytes, DEFAULT_CHARSET);
            return this.clazz == null ? (T) JSON.parse(args) : JSON.parseObject(args, this.clazz);
        } else {
            return null;
        }
    }
}
