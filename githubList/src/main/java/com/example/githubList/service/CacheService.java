package com.example.githubList.service;

import com.example.githubList.model.GitHubRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "github:repos:";
    private static final Duration CACHE_TTL = Duration.ofHours(4); // 4 saat

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheRepositories(String language, String query, List<GitHubRepository> repositories) {
        try {
            String cacheKey = buildCacheKey(language, query);
            String jsonData = objectMapper.writeValueAsString(repositories);
            redisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_TTL);
        } catch (Exception e) {
            System.err.println("Cache kaydetme hatası: " + e.getMessage());
        }
    }

    public Optional<List<GitHubRepository>> getCachedRepositories(String language, String query) {
        try {
            String cacheKey = buildCacheKey(language, query);
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                String jsonData = cachedData.toString();
                List<GitHubRepository> repositories = objectMapper.readValue(
                    jsonData, 
                    new TypeReference<List<GitHubRepository>>() {}
                );
                return Optional.of(repositories);
            }
        } catch (Exception e) {
            System.err.println("Cache okuma hatası: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    public void cacheRepositorySummary(String repoId, String summary) {
        try {
            String cacheKey = "github:summary:" + repoId;
            redisTemplate.opsForValue().set(cacheKey, summary, Duration.ofHours(2));
        } catch (Exception e) {
            System.err.println("Özet cache kaydetme hatası: " + e.getMessage());
        }
    }

    public Optional<String> getCachedSummary(String repoId) {
        try {
            String cacheKey = "github:summary:" + repoId;
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                return Optional.of(cachedData.toString());
            }
        } catch (Exception e) {
            System.err.println("Özet cache okuma hatası: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    // Belirli bir cache key'ini temizle
    public void clearCache(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
            System.out.println("Cache key temizlendi: " + cacheKey);
        } catch (Exception e) {
            System.err.println("Cache temizleme hatası: " + e.getMessage());
        }
    }

    // Tüm cache'i temizle
    public void clearAllCache() {
        try {
            // Tüm cache'i temizle
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            System.out.println("Tüm cache başarıyla temizlendi");
        } catch (Exception e) {
            System.err.println("Cache temizleme hatası: " + e.getMessage());
        }
    }

    public boolean isCacheAvailable() {
        try {
            redisTemplate.opsForValue().set("test:connection", "test", Duration.ofSeconds(10));
            redisTemplate.delete("test:connection");
            return true;
        } catch (Exception e) {
            System.err.println("Redis bağlantı hatası: " + e.getMessage());
            return false;
        }
    }

    private String buildCacheKey(String language, String query) {
        String normalizedLanguage = language != null ? language.toLowerCase().trim() : "all";
        String normalizedQuery = query != null ? query.toLowerCase().trim() : "trending";
        return CACHE_PREFIX + normalizedLanguage + ":" + normalizedQuery;
    }
}
