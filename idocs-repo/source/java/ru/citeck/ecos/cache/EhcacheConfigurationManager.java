package ru.citeck.ecos.cache;


import org.ehcache.CacheManager;

import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;

public class EhcacheConfigurationManager {

    public static long cacheSize = 10000L;
    public static long timeToLive = 600L;

    private CacheManager cacheManager;

    public void init() {
        cacheManager = newCacheManagerBuilder()
                .build(true);
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void destroy() {
        cacheManager.close();
    }
}
