package RedisPlayGround;

import redis.clients.jedis.*;

public class HelloRedis {
    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);
        try (Jedis jedis = jedisPool.getResource()){
            jedis.set("mykey", "Hello from Jedis");
            String value = jedis.get("mykey");
            System.out.println( value );
        }
        jedisPool.close();
    }
}
