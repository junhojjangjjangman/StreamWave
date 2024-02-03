package com.bipa4.back_bipatv.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

//레디스 템플릿은 사용하는 자료구조마다 제공하는 메서드가 다르기 때문에 객체를 만들어서 레디스의 자료구조 타입에 맞는 메소드를 사용하면 된다.

//메서드 명	    레디스 타입
//opsForValue	String
//opsForList	List
//opsForSet	    Set
//opsForZSet	Sorted Set
//opsForHash	Hash
@Service
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setData(String key, String value,Long expiredTime){
        redisTemplate.opsForValue().set(key, value, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getData(String key){
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteData(String key){
        redisTemplate.delete(key);
    }
}