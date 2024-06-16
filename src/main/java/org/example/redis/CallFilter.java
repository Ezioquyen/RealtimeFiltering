package org.example.redis;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;


import org.example.data.Response;
import org.example.producer.Producer;
import org.example.config.Config;
import org.example.data.Call;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;


public class CallFilter {

    private final Producer producer;
    private final JedisPool jedisPool;

    public CallFilter() {
        producer = new Producer();
        String redisHost = "localhost";
        int redisPort = 6379;
        final JedisPoolConfig poolConfig = buildPoolConfig();
        jedisPool = new JedisPool(poolConfig, redisHost, redisPort);

    }


    public void handleCall(Call call) {

        try (Jedis jedis = jedisPool.getResource()) {
            if (acquireLock(call, jedis)) {
                String dailyKey = "requests:" + call.getCaller() + ":" + getCurrentDate();
                long dailyRequests = jedis.incr(dailyKey);
                if (dailyRequests == 0) {
                    jedis.expire(dailyKey, 30);
                } else if (dailyRequests >= Config.MAX_DAILY_CALL) {
                    producer.response(new Response("Call is not allowed: reached limit", call.getSendTime()), call.getId().toString());

                    jedis.decr(dailyKey);
                    return;
                }
                producer.response(new Response("Call is allowed", call.getSendTime()), call.getId().toString());
            }


        }
    }


    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    private String getCurrentDate() {
        return LocalDate.now(ZoneId.of("UTC")).toString();
    }


    private long getSecondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atStartOfDay().plusDays(1);
        return ChronoUnit.SECONDS.between(now, endOfDay);
    }

    private boolean acquireLock(Call call, Jedis jedis) {

        String timeIntervalLock = jedis.set(call.getCaller()+":caller", "locked", SetParams.setParams().nx());
        jedis.expire(call.getCaller()+":caller", 5);
        if ("OK".equals(timeIntervalLock)) {

            String receiverLock = jedis.set(call.getCaller()+":receiver", "locked", SetParams.setParams().nx());
            jedis.expire(call.getReceiver()+":receiver", 5);
            if ("OK".equals(receiverLock)) {
                jedis.expire(call.getReceiver()+":receiver", call.getDuration());
                jedis.expire(call.getCaller()+":caller", Config.MIN_TIME_INTERVAL);
                return true;
            } else {
                producer.response(new Response("Call is not allowed: The receiver is on another call", call.getSendTime()), call.getId());
                return false;
            }
        } else
            producer.response(new Response("Call is not allowed: The interval between two calls is " + Config.MIN_TIME_INTERVAL + " seconds", call.getSendTime()), call.getId().toString());
        return false;


    }
}
