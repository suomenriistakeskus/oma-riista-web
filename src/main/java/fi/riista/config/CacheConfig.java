package fi.riista.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import fi.riista.config.profile.AmazonDatabase;
import fi.riista.config.profile.StandardDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@AmazonDatabase
@StandardDatabase
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(CacheConfig.class);

    @Resource
    private Environment env;

    @Override
    public CacheManager cacheManager() {
        return new CaffeineCacheManager() {
            // Expire cache by default in 5 minutes
            private static final long DEFAUT_CACHE_TTL_SECONDS = 60 * 5;

            @Override
            protected org.springframework.cache.Cache createCaffeineCache(final String name) {
                final long ttlSeconds = env.getProperty("cache.ttl." + name, long.class, DEFAUT_CACHE_TTL_SECONDS);
                final Long size = env.getProperty("cache.size." + name, Long.class);

                LOG.info("Creating cache {} with maxSize = {} and ttl = {}", name, size, ttlSeconds);

                final Caffeine<Object, Object> builder = Caffeine.newBuilder()
                        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS);

                if (size != null && size > 0) {
                    builder.maximumSize(size);
                }

                return new CaffeineCache(name, builder.build());
            }
        };
    }
}
