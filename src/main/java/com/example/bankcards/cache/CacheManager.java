package com.example.bankcards.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheManager implements Cache {
    private final ScheduledExecutorService scheduledExecutor;
    @Getter
    private final Map<String, CacheEntry> cacheMap = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);


    @Override
    public void scheduledDelete() {
        scheduledExecutor.scheduleAtFixedRate(this::cleanUpExpired, 60, 60, TimeUnit.MINUTES);
        scheduledExecutor.shutdown();
    }

    @Override
    public void cleanUpExpired() {
        int cleaned = counter.incrementAndGet();
        long now = System.currentTimeMillis();
        AtomicInteger removedCount = new AtomicInteger();

        cacheMap.entrySet().removeIf(entry -> {
            log.info("[INFO] Текущее время минус время добавления в кеш в минутах: [{}]",
                    (now - entry.getValue().getAddedTime()) / 1000);
            boolean shouldRemove = (now - entry.getValue().getAddedTime()) > TimeUnit.HOURS.toMillis(1);
            if(shouldRemove){
                removedCount.getAndIncrement();
            }
            return shouldRemove;
        });
        log.info("Очистка: {}, было удалено: {}, осталось записей: {}", cleaned, removedCount, cacheMap.size());
    }

    @Override
    public Object get(String key) {
        var object = cacheMap.get(key);
        if (nonNull(object)) {
            return object;
        }

        return null;
    }

    @Override
    public void put(String key, CacheEntry value) {
        log.info("[INFO] Кладём объект {} по ключу {} в кеш", value, key);
        cacheMap.computeIfAbsent(key, k -> value);
    }

    @Override
    public void delete(String key) {
        log.info("[INFO] Удаляем объект по ключу {} в кеш", key);
        cacheMap.remove(key);
    }

    @Override
    public void update(String key, CacheEntry value) {
        if (cacheMap.containsKey(key)) {
            var currentObject = cacheMap.get(key);
            if(nonNull(currentObject) && !currentObject.equals(value)) {
                cacheMap.put(key, value);
            }
        }
    }
}
