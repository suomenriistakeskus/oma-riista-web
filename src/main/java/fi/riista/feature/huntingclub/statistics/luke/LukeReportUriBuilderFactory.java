package fi.riista.feature.huntingclub.statistics.luke;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class LukeReportUriBuilderFactory extends CacheLoader<LukeReportUriBuilderFactory.CacheKey, LukeReportUriBuilder> {
    private final TransactionTemplate transactionTemplate;
    private final RequireEntityService requireEntityService;
    private final URI baseUri;

    @Override
    public LukeReportUriBuilder load(final CacheKey key) {
        return transactionTemplate.execute(transactionStatus -> {
            final HarvestPermit permit = requireEntityService.requireHarvestPermit(key.permitId, EntityPermission.READ);
            final HuntingClub club = key.clubId != null ? requireEntityService.requireHuntingClub(key.clubId, EntityPermission.READ) : null;
            return new LukeReportUriBuilder(baseUri, permit, club);
        });
    }

    public static final class CacheKey {
        private final String sessionId;
        private final long permitId;
        private final Long clubId;

        private CacheKey(final String sessionId, final long permitId, final Long clubId) {
            this.sessionId = Objects.requireNonNull(sessionId);
            this.permitId = permitId;
            this.clubId = clubId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            final CacheKey cacheKey = (CacheKey) o;
            return permitId == cacheKey.permitId &&
                    Objects.equals(sessionId, cacheKey.sessionId) &&
                    Objects.equals(clubId, cacheKey.clubId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sessionId, permitId, clubId);
        }
    }

    private final LoadingCache<CacheKey, LukeReportUriBuilder> uriBuilderCache;

    @Autowired
    public LukeReportUriBuilderFactory(final PlatformTransactionManager transactionManager,
                                       final RequireEntityService requireEntityService,
                                       final LukeReportEndpoint lukeReportEndpoint) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
        this.requireEntityService = requireEntityService;
        this.baseUri = lukeReportEndpoint.getBaseUri();
        this.uriBuilderCache = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build(LukeReportUriBuilderFactory.this);
    }

    public LukeReportUriBuilder getUriBuilder(final long permitId, final Long clubId, final HttpSession httpSession) {
        final CacheKey key = new CacheKey(httpSession.getId(), permitId, clubId);

        return uriBuilderCache.getUnchecked(key);
    }
}
