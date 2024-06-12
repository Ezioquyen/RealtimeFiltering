package org.example.redis;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

import org.example.data.Response;
import org.example.producer.Producer;
import org.example.config.Config;
import org.example.data.Call;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class CallFilter {
    private final JedisPool jedisPool;
    private static CallFilter INSTANCE;
    private Producer producer;

    private CallFilter() {
        producer = Producer.getInstance();
        String redisHost = "localhost";
        int redisPort = 6379;
        final JedisPoolConfig poolConfig = buildPoolConfig();
        this.jedisPool= new JedisPool(poolConfig, redisHost, redisPort);
    }

    public static CallFilter getInstance() {
            if (INSTANCE == null) {
                System.out.println("check-point");
                INSTANCE = new CallFilter();
            }
        return INSTANCE;
    }

    public void handleCall(Call call) {
        try (Jedis jedis = jedisPool.getResource()) {
            String dailyKey = "requests:" + call.getCaller()+ ":" + getCurrentDate();
            String timestampKey = "last_request:" + call.getCaller();

            long currentTime = System.currentTimeMillis();

            long dailyRequests = Long.parseLong(jedis.get(dailyKey));
            if (dailyRequests == 0) {
                jedis.expire(dailyKey, 86400);
            } else if (dailyRequests >= Config.MAX_DAILY_CALL) {
                producer.response(new Response("Call is not allowed: reached limit",call.getSendTime()),call.getId().toString());
                System.out.println("Call is not allowed: reached limit");
                return;
            } else {
                String lastRequestTime = jedis.get(timestampKey);
                if (lastRequestTime != null) {
                    long lastTime = Long.parseLong(lastRequestTime);
                    if (currentTime - lastTime < Config.MIN_TIME_INTERVAL) {
                        producer.response(new Response("Call is not allowed: min time interval must be under " + Config.MIN_TIME_INTERVAL / 1000 + " seconds",call.getSendTime()), call.getId().toString());
                        System.out.println("Call is not allowed: min time interval must be under " + Config.MIN_TIME_INTERVAL / 1000 + " seconds");
                        return;
                    }
                }
            }
            jedis.incr(dailyKey);
            jedis.set(timestampKey, String.valueOf(currentTime));
            producer.response(new Response("Call is allowed",call.getSendTime()),call.getId().toString());
            System.out.println("Call allowed");

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

}
