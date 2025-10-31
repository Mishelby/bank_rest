package com.example.bankcards.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Slf4j
@Configuration
public class ThreadPoolExecutorConfig {
    private static final int CORE_POOL_SIZE;
    private static final Integer MAXIMUM_CORE_PULL_SIZE;
    private static final Integer KEEP_ALIVE_TIME;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    static {
        CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() - 1;
        MAXIMUM_CORE_PULL_SIZE = CORE_POOL_SIZE * 2;
        KEEP_ALIVE_TIME = 30;
        log.info("[INFO] Количество свободных процессоров: {}", CORE_POOL_SIZE);
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(

    ) {
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_CORE_PULL_SIZE,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory());
    }

}
