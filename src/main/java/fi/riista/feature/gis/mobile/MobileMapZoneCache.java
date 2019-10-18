package fi.riista.feature.gis.mobile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fi.riista.feature.account.area.PersonalArea;
import fi.riista.feature.account.area.PersonalAreaRepository;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.HuntingClubAreaRepository;
import fi.riista.feature.moderatorarea.ModeratorArea;
import fi.riista.feature.moderatorarea.ModeratorAreaRepository;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.HarvestPermitAreaRepository;
import fi.riista.util.F;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class MobileMapZoneCache extends CacheLoader<String, Long> {
    private static final Long RESULT_NOT_FOUND = (long) -1;
    private final TransactionTemplate transactionTemplate;
    private final HuntingClubAreaRepository huntingClubAreaRepository;
    private final HarvestPermitAreaRepository harvestPermitAreaRepository;
    private final ModeratorAreaRepository moderatorAreaRepository;
    private final PersonalAreaRepository personalAreaRepository;
    private final LoadingCache<String, Long> zoneIdCache;

    @Autowired
    public MobileMapZoneCache(final PlatformTransactionManager transactionManager,
                              final HarvestPermitAreaRepository harvestPermitAreaRepository,
                              final PersonalAreaRepository personalAreaRepository,
                              final ModeratorAreaRepository moderatorAreaRepository,
                              final HuntingClubAreaRepository huntingClubAreaRepository) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
        this.harvestPermitAreaRepository = harvestPermitAreaRepository;
        this.personalAreaRepository = personalAreaRepository;
        this.moderatorAreaRepository = moderatorAreaRepository;
        this.huntingClubAreaRepository = huntingClubAreaRepository;
        this.zoneIdCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .build(MobileMapZoneCache.this);
    }

    @Override
    public Long load(final String externalId) throws Exception {
        return transactionTemplate.execute(transactionStatus -> {
            final Long zoneId = getInternalZoneId(externalId);

            // Can not return null as cache result
            return zoneId == null ? RESULT_NOT_FOUND : zoneId;
        });
    }

    private Long getInternalZoneId(final String externalId) {
        final Optional<HuntingClubArea> clubAreaOptional = huntingClubAreaRepository.findByExternalId(externalId);

        if (clubAreaOptional.isPresent()) {
            return F.getId(clubAreaOptional.get().getZone());
        }

        final Optional<HarvestPermitArea> permitAreaOptional = harvestPermitAreaRepository.findByExternalId(externalId);

        if (permitAreaOptional.isPresent()) {
            return F.getId(permitAreaOptional.get().getZone());
        }

        final Optional<PersonalArea> personalAreaOptional = personalAreaRepository.findByExternalId(externalId);

        if (personalAreaOptional.isPresent()) {
            return F.getId(personalAreaOptional.get().getZone());
        }

        final Optional<ModeratorArea> moderatorAreaOptional = moderatorAreaRepository.findByExternalId(externalId);

        if (moderatorAreaOptional.isPresent()) {
            return F.getId(moderatorAreaOptional.get().getZone());
        }

        return null;
    }

    public Long findByExternalId(String externalId) {
        final Long zoneId = zoneIdCache.getUnchecked(externalId);
        return zoneId >= 0 ? zoneId : null;
    }
}
