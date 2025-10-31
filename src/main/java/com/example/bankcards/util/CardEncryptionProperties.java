package com.example.bankcards.util;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "card.encryption")
@Getter
@Setter
public class CardEncryptionProperties {
    private String key;
    private String iv;

    @PostConstruct
    public  void init() {
        log.info("[INFO] Init key and iv value: {}, {}", key, iv);
    }
}
