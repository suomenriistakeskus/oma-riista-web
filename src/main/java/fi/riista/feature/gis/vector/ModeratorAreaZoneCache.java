package fi.riista.feature.gis.vector;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.moderatorarea.ModeratorAreaRepository;
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
public class ModeratorAreaZoneCache {
    private final TransactionTemplate transactionTemplate;
    private final ActiveUserService activeUserService;
    private final ModeratorAreaRepository moderatorAreaRepository;

    // Key = <sessionId, applicationId> Value = has read permission
    private final LoadingCache<Tuple2<String, Long>, Boolean> authorizationCache;
    private final LoadingCache<Long, Long> zoneIdCache;

    @Autowired
    public ModeratorAreaZoneCache(final PlatformTransactionManager transactionManager,
                                  final ActiveUserService activeUserService,
                                  final ModeratorAreaRepository moderatorAreaRepository) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
        this.activeUserService = activeUserService;
        this.moderatorAreaRepository = moderatorAreaRepository;
        this.zoneIdCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
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

    public Long getZoneId(long areaId, final HttpSession httpSession) {
        return hasReadPermissionCached(areaId, httpSession) ? zoneIdCache.getUnchecked(areaId) : null;
    }

    private boolean hasReadPermissionCached(long areaId, final HttpSession httpSession) {
        return authorizationCache.getUnchecked(Tuple.of(httpSession.getId(), areaId));
    }

    private boolean hasReadPermission(final Tuple2<String, Long> key) {
        final ModeratorArea area = moderatorAreaRepository.getOne(key._2());
        return activeUserService.checkHasPermission(area, EntityPermission.READ);
    }

    private Long getInternalZoneId(final Long areaId) {
        return Optional
                .ofNullable(moderatorAreaRepository.getOne(areaId))
                .map(ModeratorArea::getZone)
                .map(GISZone::getId)
                .orElse(null);
    }
}
