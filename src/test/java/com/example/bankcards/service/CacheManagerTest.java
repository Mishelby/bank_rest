package com.example.bankcards.service;

import com.example.bankcards.cache.CacheEntry;
import com.example.bankcards.cache.CacheManager;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class CacheManagerTest {
    @InjectMocks
    private CacheManager cacheManager;

    @Mock
    private final Map<String, CacheEntry> cacheMap = new ConcurrentHashMap<>();

    @Mock
    private final AtomicInteger counter = new AtomicInteger(0);

    @Mock
    private ScheduledExecutorService scheduledExecutor;

    private CardEntity cardEntityOne;
    private CardEntity cardEntityTwo;

    private CacheEntry cacheEntryOne;
    private CacheEntry cacheEntryTwo;

    private static final LocalDate expiredDate = LocalDate.now().plusYears(1L);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cardEntityOne = new CardEntity();
        cardEntityOne.setId(1L);
        cardEntityOne.setExpirationDate(expiredDate);
        cardEntityOne.setCardStatus(CardStatus.BLOCKED);
        cardEntityOne.setNumber("1234 5678 9012 3456");

        cardEntityTwo = new CardEntity();
        cardEntityTwo.setId(2L);
        cardEntityTwo.setExpirationDate(expiredDate);
        cardEntityTwo.setCardStatus(CardStatus.ACTIVE);
        cardEntityTwo.setNumber("1234 5734 9012 1953");

        cacheEntryOne = CacheEntry.builder()
                .payload(cardEntityOne)
                .addedTime(System.currentTimeMillis())
                .build();

        cacheEntryTwo = CacheEntry.builder()
                .payload(cardEntityTwo)
                .addedTime(System.currentTimeMillis())
                .build();
    }

    @Test
    void testAddCacheEntryToMap() {
//        when(cacheManager.getCacheMap()).thenReturn(cacheMap);

        cacheManager.put(cardEntityOne.getId().toString(), cacheEntryOne);

        when(cacheMap.get(cardEntityOne.getId().toString())).thenReturn(cacheEntryOne);

//        verify(cacheManager).put(anyString(), any(CacheEntry.class));

        assertEquals(cacheMap.get((cardEntityOne.getId().toString())), cacheEntryOne, "Объект кеша должен совпадать");
        assertNotNull(cacheMap.get((cardEntityOne.getId().toString())), "Объект не должен быть равен null");
    }

    @Test
    void deleteCacheFromCacheManager() {
        cacheManager.put(cardEntityOne.getId().toString(), cacheEntryOne);

        when(cacheMap.get(cardEntityOne.getId().toString())).thenReturn(cacheEntryOne);

        assertEquals(cacheMap.get((cardEntityOne.getId().toString())), cacheEntryOne, "Объект кеша должен совпадать");

        cacheManager.delete(cardEntityOne.getId().toString());

        assertNull(cacheManager.get(cardEntityOne.getId().toString()), "Объект должен быть null");
    }

    @Test
    void updateNonEqualsObjectInCache(){
        cacheManager.put(cardEntityOne.getId().toString(), cacheEntryOne);

        when(cacheManager.get(cardEntityOne.getId().toString())).thenReturn(cacheEntryOne);

        assertEquals(cacheManager.get((cardEntityOne.getId().toString())), cacheEntryOne, "Объект кеша должен совпадать");

        cacheManager.update(cardEntityOne.getId().toString(), cacheEntryTwo);

        assertNotEquals(cacheManager.get(cardEntityTwo.getId().toString()), cardEntityOne, "Объект кеша не должен совпадать");
    }

    @Test
    void updateEqualsObjectInCache(){
        cacheManager.put(cardEntityOne.getId().toString(), cacheEntryOne);

        when(cacheManager.get(cardEntityOne.getId().toString())).thenReturn(cacheEntryOne);

        assertEquals(cacheManager.get((cardEntityOne.getId().toString())), cacheEntryOne, "Объект кеша должен совпадать");

        cacheManager.update(cardEntityOne.getId().toString(), cacheEntryOne);

        assertEquals(cacheManager.get(cardEntityOne.getId().toString()), cacheEntryOne, "Объект кеша должен совпадать");
    }

    @Test
    void cleanUpExpiredObjectInCache(){
        cacheEntryOne = CacheEntry.builder()
                .payload(cardEntityOne)
                .addedTime(System.currentTimeMillis() - (36000 * 600))
                .build();

        cacheManager.put(cardEntityOne.getId().toString(), cacheEntryOne);

//        when(cacheManager.getCacheMap()).thenReturn(cacheMap);

        assertEquals(cacheManager.get((cardEntityOne.getId().toString())), cacheEntryOne, "Объект кеша должен совпадать");


        cacheManager.cleanUpExpired();

        assertNull(cacheManager.get(cardEntityOne.getId().toString()), "Объект должен был быть удалён из кеша");
    }

}
