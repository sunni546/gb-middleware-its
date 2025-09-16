//package com.middleware.common.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
////@Configuration
//@EnableRedisRepositories
//@RequiredArgsConstructor
//public class RedisConfig {
//
//  private final RedisProperties redisProperties;
//
//  @Bean
//  public RedisConnectionFactory redisConnectionFactory() {
//    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
//    redisConfig.setHostName(redisProperties.getHost());
//    redisConfig.setPort(redisProperties.getPort());
//    redisConfig.setPassword(redisProperties.getPassword());
//    return new LettuceConnectionFactory(redisConfig);
//  }
//
//  @Bean
//  public RedisConnection redisConnection() {
//    return redisConnectionFactory().getConnection();
//  }
//
//  @Bean
//  public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//    RedisTemplate<?, ?> template = new RedisTemplate<>();
//    template.setConnectionFactory(redisConnectionFactory);
//    template.setKeySerializer(new StringRedisSerializer()); // Key는 String
//    template.setHashKeySerializer(new StringRedisSerializer()); // Hash Key는 String
//    template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // Value는 JSON 직렬화
//    template.setEnableTransactionSupport(true);
//
//    return template;
//  }
//}