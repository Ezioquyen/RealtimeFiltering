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
    private static CallFilter INSTANCE;
    private final Producer producer;
   private final  JedisPool jedisPool;
    private CallFilter() {
        producer = Producer.getInstance();
        String redisHost = "localhost";
        int redisPort = 6379;
        final JedisPoolConfig poolConfig = buildPoolConfig();
        jedisPool = new JedisPool(poolConfig,redisHost,redisPort);


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
            if (acquireLock(call.getCaller(),jedis)) {
                String dailyKey = "requests:" + call.getCaller() + ":" + getCurrentDate();
                long dailyRequests = jedis.incr(dailyKey);
                if (dailyRequests == 0) {
                    jedis.expire(dailyKey, getSecondsUntilEndOfDay());
                } else if (dailyRequests >= Config.MAX_DAILY_CALL) {
                    producer.response(new Response("Call is not allowed: reached limit", call.getSendTime()), call.getId().toString());
                    System.out.println("Call is not allowed: reached limit");
                    jedis.decr(dailyKey);
                    return;
                }
                producer.response(new Response("Call is allowed", call.getSendTime()), call.getId().toString());
                System.out.println("Call allowed");
                return;
            }
            producer.response(new Response("Call is not allowed: The interval between two calls is " + Config.MIN_TIME_INTERVAL / 1000 + " seconds", call.getSendTime()), call.getId().toString());
            System.out.println("Call is not allowed: The interval between two calls is 30 seconds " + Config.MIN_TIME_INTERVAL / 1000 + " seconds");
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

    private boolean acquireLock(String phone, Jedis jedis) {
        String result = jedis.set(phone,"locked", SetParams.setParams().nx());
        if("OK".equals(result)){
            jedis.expire(phone, Config.MIN_TIME_INTERVAL/1000);
            return true;
        }
        return false;
    }
}
