package com.ticketya.catalogo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String CACHE_EVENTOS_DETALLE = "eventosDetalle";
    public static final String CACHE_LUGARES = "lugares";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_EVENTOS_DETALLE, CACHE_LUGARES);
    }

    @Scheduled(fixedRate = 300000)
    public void expirarCachesPorTtl() {
        CacheManager cm = cacheManager();
        for (String nombre : cm.getCacheNames()) {
            Cache cache = cm.getCache(nombre);
            if (cache != null) {
                cache.clear();
            }
        }
        log.debug("[CACHE_TTL] Caches de catalogo limpiados por expiracion de TTL (5 min)");
    }
}
