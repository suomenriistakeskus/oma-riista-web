package fi.riista.feature.sms.delivery;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenQuotaLimiterTest {

    @Test
    public void testSendMessageWithQuotaOneSucceeds() {
        TokenQuotaLimiter limiter = new TokenQuotaLimiter(60, TimeUnit.MINUTES, 1);
        assertTrue(limiter.acquire("test"));
    }

    @Test
    public void testSendSecondMessageWithQuotaOneFails() {
        TokenQuotaLimiter limiter = new TokenQuotaLimiter(60, TimeUnit.MINUTES, 1);
        assertTrue(limiter.acquire("test"));
        assertFalse(limiter.acquire("test"));
        assertFalse(limiter.acquire("test"));
    }

    @Test
    public void testSendSecondMessageWithQuotaTwoSucceeds() {
        TokenQuotaLimiter limiter = new TokenQuotaLimiter(60, TimeUnit.MINUTES, 2);
        assertTrue(limiter.acquire("test"));
        assertTrue(limiter.acquire("test"));
    }

    @Test
    public void testCacheExpirationTimeUpdatedOnlyOnSuccessfulAcquire() throws InterruptedException {
        final int tokenExpirationTimeMillis = 20;

        TokenQuotaLimiter limiter = new TokenQuotaLimiter(tokenExpirationTimeMillis, TimeUnit.MILLISECONDS, 2);

        assertTrue(limiter.acquire("test"));
        assertTrue(limiter.acquire("test"));
        assertFalse(limiter.acquire("test"));

        long expirationTime = System.currentTimeMillis() + tokenExpirationTimeMillis;

        // Loop calls to acquire, but leave few millis gap
        // If you loop all the way to expirationTime, next acquire might actually be able to acquire. Therefore,
        // leave tiny gap to the end.
        long gap = 10; // 5 is not enough
        while (System.currentTimeMillis() < expirationTime - gap) {
            assertFalse("Should not be able to acquire", limiter.acquire("test"));
        }

        // wait for the expirationTime
        while (System.currentTimeMillis() < expirationTime) {
            // do nothing, this is just waiting
        }

        // make sure limiter will clean its cache
        limiter.cleanUp();

        // make sure cache cleanup has time to run
        // tricky to sleep enough to make it pass when running mvn test, but not too long to actually test that
        // failed acquire won't update cache expiration time
        Thread.sleep(tokenExpirationTimeMillis - 5);

        assertTrue(limiter.acquire("test"));
    }

}
