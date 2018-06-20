package fi.riista.feature.permit.application.geometry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.security.EntityPermission;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class HarvestPermitApplicationZoneCache {
    private final TransactionTemplate transactionTemplate;
    private final ActiveUserService activeUserService;
    private final HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    // Key = <sessionId, applicationId> Value = has read permission
    private final LoadingCache<Tuple2<String, Long>, Boolean> authorizationCache;
    private final LoadingCache<Long, Long> zoneIdCache;

    @Autowired
    public HarvestPermitApplicationZoneCache(final PlatformTransactionManager transactionManager,
                                             final ActiveUserService activeUserService,
                                             final HarvestPermitApplicationRepository harvestPermitApplicationRepository) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.activeUserService = activeUserService;
        this.harvestPermitApplicationRepository = harvestPermitApplicationRepository;
        this.zoneIdCache = CacheBuilder
                .newBuilder()
                .build(new CacheLoader<Long, Long>() {
                    @Override
                    public Long load(final Long applicationId) throws Exception {
                        return transactionTemplate.execute(transactionStatus -> getInternalZoneId(applicationId));
                    }
                });
        this.authorizationCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<Tuple2<String, Long>, Boolean>() {
                    @Override
                    public Boolean load(final Tuple2<String, Long> key) throws Exception {
                        return transactionTemplate.execute(transactionStatus -> hasReadPermission(key));
                    }
                });
    }

    public Long getZoneId(long applicationId, final HttpSession httpSession) {
        return hasReadPermissionCached(applicationId, httpSession) ? zoneIdCache.getUnchecked(applicationId) : null;
    }

    private boolean hasReadPermissionCached(long applicationId, final HttpSession httpSession) {
        return authorizationCache.getUnchecked(Tuple.of(httpSession.getId(), applicationId));
    }

    private boolean hasReadPermission(final Tuple2<String, Long> key) {
        final HarvestPermitApplication application = harvestPermitApplicationRepository.getOne(key._2());
        return activeUserService.checkHasPermission(application, EntityPermission.READ);
    }

    private Long getInternalZoneId(final Long applicationId) {
        return Optional
                .ofNullable(harvestPermitApplicationRepository.getOne(applicationId))
                .map(HarvestPermitApplication::getArea)
                .map(HarvestPermitArea::getZone)
                .map(GISZone::getId)
                .orElse(null);
    }
}
