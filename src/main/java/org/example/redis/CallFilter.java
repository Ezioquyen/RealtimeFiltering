package org.example.redis;

import java.time.Duration;
import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class CallFilter {
    private int count;
    private final JedisPool jedisPool;
    private static CallFilter INSTANCE;


    private CallFilter() {
        String redisHost = "localhost";
        int redisPort = 6379;
        count = 0;
        final JedisPoolConfig poolConfig = buildPoolConfig();
        this.jedisPool= new JedisPool(poolConfig, redisHost, redisPort);
    }

    public static CallFilter getInstance() {
        synchronized (CallFilter.class) {
            if (INSTANCE == null) {
                System.out.println("check-point");
                INSTANCE = new CallFilter();
            }
        }
        return INSTANCE;
    }

    public boolean isCallAllowed(String phoneNumber, int maxCalls, int periodInSeconds) throws InterruptedException {
        String key = "calls:" + phoneNumber;
        long currentTime = System.currentTimeMillis() / 1000;


        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("This message from thread " + Thread.currentThread().threadId() + " by " + count++);// thao tác với Redis
            List<String> timestamps = jedis.lrange(key, 0, -1);

            List<String> validTimestamps = timestamps.stream()
                    .filter(ts -> (currentTime - Long.parseLong(ts)) < periodInSeconds)
                    .toList();

            if (validTimestamps.size() >= maxCalls) {
                return false;
            }

            jedis.lpush(key, String.valueOf(currentTime));
            jedis.ltrim(key, 0, maxCalls - 1);
        }

        return true;
    }

    public void handleCall(String phoneNumber) throws InterruptedException {
        int maxCalls = 30;
        int periodInSeconds = 60;

        if (isCallAllowed(phoneNumber, maxCalls, periodInSeconds)) {
            System.out.println("Call allowed");
        } else {
            System.out.println("Not allowed");
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

}
