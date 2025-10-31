package com.example.bankcards.cache;

public interface Cache {
    void scheduledDelete();
    Object get(String key);
    void put(String key, CacheEntry value);
    void delete(String key);
    void update(String key, CacheEntry value);
    void cleanUpExpired();
}
