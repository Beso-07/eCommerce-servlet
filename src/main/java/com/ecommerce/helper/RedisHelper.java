package com.ecommerce.helper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisHelper {
    private static final String HOST = System.getenv().getOrDefault("ECOMMERCE_REDIS_HOST", "localhost");
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("ECOMMERCE_REDIS_PORT", "6379"));
    private static final JedisPool POOL = new JedisPool(new JedisPoolConfig(), HOST, PORT);

    private RedisHelper() {
    }

    public static String get(String key) {
        try (Jedis jedis = POOL.getResource()) {
            return jedis.get(key);
        }
    }

    public static void setex(String key, int ttlSeconds, String value) {
        try (Jedis jedis = POOL.getResource()) {
            jedis.setex(key, ttlSeconds, value);
        }
    }

    public static void del(String key) {
        try (Jedis jedis = POOL.getResource()) {
            jedis.del(key);
        }
    }

    public static boolean incrementWithLimit(String key, int ttlSeconds, int limit) {
        try (Jedis jedis = POOL.getResource()) {
            long count = jedis.incr(key);
            if (count == 1L) {
                jedis.expire(key, ttlSeconds);
            }
            return count <= limit;
        }
    }
}
