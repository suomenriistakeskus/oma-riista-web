package fi.riista.feature.sms.delivery;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Simple rate limiter, which works as a safety check for
 * expensive operations like SMS Gateway usage. Entries are removed automatically
 * after no update/write activity. Usage counter is not allowed
 * to exceed quota for any acquired tokens.
 */
public class TokenQuotaLimiter {
    private final Cache<String, Integer> cache;
    private final int quotaSize;

    public TokenQuotaLimiter(long tokenExpirationTime, TimeUnit timeUnit, int quotaSize) {
        Preconditions.checkArgument(quotaSize > 0);
        Preconditions.checkArgument(tokenExpirationTime > 0);

        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(tokenExpirationTime, timeUnit)
                .build();
        this.quotaSize = quotaSize;
    }

    public boolean acquire(final String token) {
        Preconditions.checkArgument(StringUtils.isNotBlank(token));

        final Integer currentValue = cache.getIfPresent(token);

        if (currentValue == null) {
            cache.put(token, 1);

            return true;
        }

        final int nextValue = currentValue + 1;

        boolean isAcquired = nextValue <= quotaSize;
        if (isAcquired) {
            cache.put(token, nextValue);
        }
        return isAcquired;
    }

    /**
     * For testing
     */
    void cleanUp() {
        cache.cleanUp();
    }
}
