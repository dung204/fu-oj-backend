package com.example.modules.redis.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public void set(@NonNull String key, @NonNull Object value) {
    try {
      redisTemplate.opsForValue().set(key, value);
      log.debug("Set key: {} with value type: {}", key, value.getClass().getSimpleName());
    } catch (Exception e) {
      log.error("Error setting key {}: {}", key, e.getMessage());
      throw new RuntimeException("Failed to set Redis key: " + key, e);
    }
  }

  public void set(@NonNull String key, @NonNull Object value, @NonNull Duration timeout) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout);
      log.debug(
        "Set key: {} with value type: {} and timeout: {}",
        key,
        value.getClass().getSimpleName(),
        timeout
      );
    } catch (Exception e) {
      log.error("Error setting key {} with timeout: {}", key, e.getMessage());
      throw new RuntimeException("Failed to set Redis key with timeout: " + key, e);
    }
  }

  public <T> T get(@NonNull String key, Class<T> type) {
    try {
      Object value = redisTemplate.opsForValue().get(key);
      if (value == null) {
        log.debug("Key not found: {}", key);
        return null;
      }

      return objectMapper.convertValue(value, type);
    } catch (Exception e) {
      log.error("Error getting key {}: {}", key, e.getMessage());
      return null;
    }
  }

  public Boolean delete(@NonNull String key) {
    try {
      Boolean result = redisTemplate.delete(key);
      log.debug("Deleted key: {} (existed: {})", key, result);
      return result;
    } catch (Exception e) {
      log.error("Error deleting key {}: {}", key, e.getMessage());
      return false;
    }
  }

  public Boolean exists(@NonNull String key) {
    try {
      return redisTemplate.hasKey(key);
    } catch (Exception e) {
      log.error("Error checking existence of key {}: {}", key, e.getMessage());
      return false;
    }
  }

  public Boolean expire(@NonNull String key, @NonNull Duration timeout) {
    try {
      return redisTemplate.expire(key, timeout);
    } catch (Exception e) {
      log.error("Error setting expiration for key {}: {}", key, e.getMessage());
      return false;
    }
  }

  // Hash operations
  public void hashSet(@NonNull String key, @NonNull String hashKey, @NonNull Object value) {
    try {
      redisTemplate.opsForHash().put(key, hashKey, value);
      log.debug("Set hash key: {}:{}", key, hashKey);
    } catch (Exception e) {
      log.error("Error setting hash key {}:{}: {}", key, hashKey, e.getMessage());
      throw new RuntimeException("Failed to set Redis hash key", e);
    }
  }

  public <T> T hashGet(String key, String hashKey, Class<T> type) {
    try {
      Object value = redisTemplate.opsForHash().get(key, hashKey);
      if (value == null) {
        return null;
      }

      return objectMapper.convertValue(value, type);
    } catch (Exception e) {
      log.error("Error getting hash key {}:{}: {}", key, hashKey, e.getMessage());
      return null;
    }
  }

  public Map<String, Object> hashGetAll(String key) {
    try {
      Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
      return entries
        .entrySet()
        .stream()
        .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
    } catch (Exception e) {
      log.error("Error getting all hash entries for key {}: {}", key, e.getMessage());
      return Map.of();
    }
  }

  public Boolean hashDelete(String key, String hashKey) {
    try {
      Long result = redisTemplate.opsForHash().delete(key, hashKey);
      return result > 0;
    } catch (Exception e) {
      log.error("Error deleting hash key {}:{}: {}", key, hashKey, e.getMessage());
      return false;
    }
  }

  // List operations
  public void listPush(String key, Object value) {
    try {
      redisTemplate.opsForList().rightPush(key, value);
      log.debug("Pushed to list key: {}", key);
    } catch (Exception e) {
      log.error("Error pushing to list key {}: {}", key, e.getMessage());
      throw new RuntimeException("Failed to push to Redis list", e);
    }
  }

  public <T> T listPop(String key, Class<T> type) {
    try {
      Object value = redisTemplate.opsForList().leftPop(key);
      if (value == null) {
        return null;
      }

      return objectMapper.convertValue(value, type);
    } catch (Exception e) {
      log.error("Error popping from list key {}: {}", key, e.getMessage());
      return null;
    }
  }

  public <T> List<T> listRange(String key, long start, long end, Class<T> type) {
    try {
      List<Object> values = redisTemplate.opsForList().range(key, start, end);
      if (values == null) {
        return List.of();
      }

      return values
        .stream()
        .map(value -> objectMapper.convertValue(value, type))
        .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error getting list range for key {}: {}", key, e.getMessage());
      return List.of();
    }
  }

  // Set operations
  public void setAdd(String key, Object value) {
    try {
      redisTemplate.opsForSet().add(key, value);
      log.debug("Added to set key: {}", key);
    } catch (Exception e) {
      log.error("Error adding to set key {}: {}", key, e.getMessage());
      throw new RuntimeException("Failed to add to Redis set", e);
    }
  }

  public <T> Set<T> setMembers(String key, Class<T> type) {
    try {
      Set<Object> values = redisTemplate.opsForSet().members(key);
      if (values == null) {
        return Set.of();
      }

      return values
        .stream()
        .map(value -> objectMapper.convertValue(value, type))
        .collect(Collectors.toSet());
    } catch (Exception e) {
      log.error("Error getting set members for key {}: {}", key, e.getMessage());
      return Set.of();
    }
  }

  public Boolean setRemove(String key, Object value) {
    try {
      Long result = redisTemplate.opsForSet().remove(key, value);
      return result != null && result > 0;
    } catch (Exception e) {
      log.error("Error removing from set key {}: {}", key, e.getMessage());
      return false;
    }
  }

  // Utility methods
  public Set<String> keys(String pattern) {
    try {
      return redisTemplate.keys(pattern);
    } catch (Exception e) {
      log.error("Error getting keys with pattern {}: {}", pattern, e.getMessage());
      return Set.of();
    }
  }
}
