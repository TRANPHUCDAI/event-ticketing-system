package com.eventticket.eventbooking.seat.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public DebugController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @GetMapping("/redis-test")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test 1: Set a value
            String testKey = "debug_test_key";
            String testValue = "debug_test_value";
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofMinutes(1));
            result.put("set", "SUCCESS");
            
            // Test 2: Get the value
            String retrieved = redisTemplate.opsForValue().get(testKey);
            result.put("get", retrieved);
            
            // Test 3: setIfAbsent (like seat hold)
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent("test_lock", "user1", Duration.ofMinutes(1));
            result.put("setIfAbsent_first", lockAcquired);
            
            // Test 4: setIfAbsent again (should fail)
            Boolean lockAcquired2 = redisTemplate.opsForValue().setIfAbsent("test_lock", "user2", Duration.ofMinutes(1));
            result.put("setIfAbsent_second", lockAcquired2);
            
            // Test 5: Delete
            redisTemplate.delete(testKey);
            redisTemplate.delete("test_lock");
            result.put("delete", "SUCCESS");
            
            result.put("overall", "ALL_TESTS_PASSED");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("exception", e.getClass().getName());
            return ResponseEntity.status(500).body(result);
        }
    }
}
