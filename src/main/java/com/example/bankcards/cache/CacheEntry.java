package com.example.bankcards.cache;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CacheEntry {
    private Object payload;
    private long addedTime;
}
