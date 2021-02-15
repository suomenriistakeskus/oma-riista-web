package fi.riista.feature.huntingclub.area;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.service.LastModifierService;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static fi.riista.util.Collect.groupingByIdOf;
import static fi.riista.util.Collect.idSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

@Component
public class HuntingClubAreaDTOTransformer extends ListTransformer<HuntingClubArea, HuntingClubAreaDTO> {

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private GISZoneRepository zoneRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private LastModifierService lastModifierService;

    @PersistenceContext
    private EntityManager entityManager;

    private final LoadingCache<Long, Boolean> changedZoneCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, Boolean>() {
                @Override
                public Boolean load(final Long id) {
                    return gisQueryService.findZonesWithChanges(singleton(id)).get(id);
                }

                @Override
                public Map<Long, Boolean> loadAll(final Iterable<? extends Long> keys) {
                    final Set<Long> ids = F.stream(keys).collect(toSet());
                    return gisQueryService.findZonesWithChanges(ids);
                }
            });

    @Nonnull
    @Override
    protected List<HuntingClubAreaDTO> transform(@Nonnull final List<HuntingClubArea> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final boolean activeUserIsAdminOrModerator = activeUser.isModeratorOrAdmin();
        final Map<Long, List<Occupation>> occupationByOrganisationId = findValidActiveUserOccupations(activeUser);

        final Function<HuntingClubArea, HuntingClub> areaToClubMapping = getAreaToClubMapping(list);
        final Function<HuntingClubArea, GISZoneWithoutGeometryDTO> areaToZoneMapping = createAreaSizeMapping(list);
        final Map<Long, Boolean> zoneChanges = findChangedZonesForAreas(list);
        final List<Long> attachedAreas = listAreaWithAttachedGroup(list);

        final Map<HuntingClubArea, LastModifierDTO> lastModifierMapping = lastModifierService.getLastModifiers(list);

        return F.mapNonNullsToList(list, area -> {

            final HuntingClub huntingClub = areaToClubMapping.apply(area);
            final GISZoneWithoutGeometryDTO zone = areaToZoneMapping.apply(area);

            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, huntingClub, zone);

            final LastModifierDTO modifier = lastModifierMapping.get(area);

            dto.setLastModifiedDate(modifier.getTimestampAsLocalDateTime());
            dto.setLastModifierName(modifier.getFullName());
            dto.setLastModifierRiistakeskus(modifier.isAdminOrModerator());

            dto.setHasPendingZoneChanges(zoneChanges.getOrDefault(F.getId(zone), false));
            dto.setAttachedToGroup(attachedAreas.contains(area.getId()));

            if (activeUserIsAdminOrModerator) {
                dto.setCanEdit(true);
            } else {
                resolvePermissions(occupationByOrganisationId, area, dto);
            }

            return dto;
        });
    }

    private Function<HuntingClubArea, GISZoneWithoutGeometryDTO> createAreaSizeMapping(final Iterable<HuntingClubArea> iterable) {
        final Set<Long> zoneIds = F.stream(iterable).map(HuntingClubArea::getZone).collect(idSet());
        final Map<Long, GISZoneWithoutGeometryDTO> mapping = zoneRepository.fetchWithoutGeometry(zoneIds);
        return a -> a.getZone() != null ? mapping.get(F.getId(a.getZone())) : null;
    }

    private List<Long> listAreaWithAttachedGroup(final Collection<HuntingClubArea> list) {
        final QHuntingClubGroup huntingClubGroup = QHuntingClubGroup.huntingClubGroup;
        final QHuntingClubArea huntingArea = huntingClubGroup.huntingArea;

        return new JPAQuery<>(entityManager)
                .from(huntingClubGroup)
                .where(huntingArea.id.isNotNull().and(huntingArea.in(list)))
                .select(huntingArea.id)
                .distinct()
                .fetch();
    }

    private Function<HuntingClubArea, HuntingClub> getAreaToClubMapping(final Iterable<HuntingClubArea> areas) {
        return CriteriaUtils.singleQueryFunction(areas, HuntingClubArea::getClub, huntingClubRepository, false);
    }

    private Map<Long, List<Occupation>> findValidActiveUserOccupations(final SystemUser activeUser) {
        final Person person = activeUser.getPerson();

        return person == null
                ? emptyMap()
                : findClubOccupations(person).stream().collect(groupingByIdOf(Occupation::getOrganisation));
    }

    private List<Occupation> findClubOccupations(final Person person) {
        return occupationRepository.findActiveByPersonAndOrganisationTypes(person, EnumSet.of(OrganisationType.CLUB));
    }

    private static void resolvePermissions(final Map<Long, List<Occupation>> occupationByOrganisationId,
                                           final HuntingClubArea area,
                                           final HuntingClubAreaDTO dto) {

        final Long clubId = F.getId(area.getClub());

        if (clubId == null || occupationByOrganisationId.isEmpty()) {
            return;
        }

        final List<Occupation> clubOccupations = occupationByOrganisationId.getOrDefault(clubId, emptyList());

        dto.setCanEdit(hasAnyOccupationWithType(clubOccupations, EnumSet.of(OccupationType.SEURAN_YHDYSHENKILO)));
    }

    private static boolean hasAnyOccupationWithType(final Collection<Occupation> occupations,
                                                    final EnumSet<OccupationType> occupationType) {

        return occupations.stream().anyMatch(o -> occupationType.contains(o.getOccupationType()));
    }

    private Map<Long, Boolean> findChangedZonesForAreas(final Collection<HuntingClubArea> areas) {
        try {
            final Set<Long> zoneIds = areas.stream().map(HuntingClubArea::getZone).collect(idSet());
            return changedZoneCache.getAll(zoneIds);

        } catch (final ExecutionException ee) {
            throw new RuntimeException(ee);
        }
    }
}
