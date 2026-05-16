// package com.dannycode.config;

// import io.github.bucket4j.distributed.proxy.ProxyManager;
// import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
// import io.lettuce.core.RedisClient;
// import io.lettuce.core.RedisURI;
// import io.lettuce.core.api.StatefulRedisConnection;
// import io.lettuce.core.codec.ByteArrayCodec;
// import io.lettuce.core.codec.RedisCodec;
// import io.lettuce.core.codec.StringCodec;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RateLimitConfig {

//     @Value("${spring.data.redis.host}")
//     private String redisHost;

//     @Value("${spring.data.redis.port}")
//     private int redisPort;

//     @Bean
//     public StatefulRedisConnection<String, byte[]> redisConnection() {
//         RedisClient client = RedisClient.create(
//                 RedisURI.builder()
//                         .withHost(redisHost)
//                         .withPort(redisPort)
//                         .build()
//         );
//         return client.connect(
//                 RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
//         );
//     }

//     @Bean
//     public ProxyManager<String> proxyManager(
//             StatefulRedisConnection<String, byte[]> redisConnection) {
//         return LettuceBasedProxyManager.builderFor(redisConnection)
//                 .build();
//     }
// }




// package com.dannycode.config;

// import io.github.bucket4j.distributed.proxy.ProxyManager;
// // import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
// import io.github.bucket4j.grid.redis.lettuce.cas.LettuceBasedProxyManager;
// import io.lettuce.core.RedisClient;
// import io.lettuce.core.RedisURI;
// import io.lettuce.core.api.StatefulRedisConnection;
// import io.lettuce.core.codec.ByteArrayCodec;
// import io.lettuce.core.codec.RedisCodec;
// import io.lettuce.core.codec.StringCodec;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RateLimitConfig {

//     @Value("${spring.data.redis.host}")
//     private String redisHost;

//     @Value("${spring.data.redis.port}")
//     private int redisPort;

//     @Bean(destroyMethod = "close")
//     public RedisClient redisClient() {
//         return RedisClient.create(
//                 RedisURI.builder()
//                         .withHost(redisHost)
//                         .withPort(redisPort)
//                         .build()
//         );
//     }

//     @Bean(destroyMethod = "close")
//     public StatefulRedisConnection<String, byte[]> redisConnection(
//             RedisClient redisClient
//     ) {

//         return redisClient.connect(
//                 RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
//         );
//     }

//     @Bean
//     public ProxyManager<String> proxyManager(
//             StatefulRedisConnection<String, byte[]> redisConnection
//     ) {

//         return LettuceBasedProxyManager
//                 .builderFor(redisConnection)
//                 .build();
//     }
// }




package com.dannycode.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean(destroyMethod = "close")
    public RedisClient redisClient() {
        return RedisClient.create(
                RedisURI.builder()
                        .withHost(redisHost)
                        .withPort(redisPort)
                        .build()
        );
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> redisConnection(
            RedisClient redisClient
    ) {

        return redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );
    }

    @Bean
    public ProxyManager<String> proxyManager(
            StatefulRedisConnection<String, byte[]> redisConnection
    ) {

        return LettuceBasedProxyManager
                .builderFor(redisConnection)
                .build();
    }
}