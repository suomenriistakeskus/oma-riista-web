package fi.riista.feature.huntingclub.area;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.GISQueryService;
import fi.riista.feature.gis.zone.AbstractAreaDTOTransformer;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class HuntingClubAreaDTOTransformer extends AbstractAreaDTOTransformer<HuntingClubArea, HuntingClubAreaDTO> {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GISQueryService gisQueryService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private PersonRepository personRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Cache query result because it will always execute full table scan.
    // Query result is only changed after manual SQL update recalculating changed geometries.
    private Supplier<Set<Long>> changedZoneCache = Suppliers.memoizeWithExpiration(
            () -> ImmutableSet.copyOf(gisQueryService.findZonesWithChanges()), 30, TimeUnit.MINUTES);

    @Nonnull
    @Override
    protected List<HuntingClubAreaDTO> transform(@Nonnull final List<HuntingClubArea> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final SystemUser activeUser = activeUserService.getActiveUser();
        final Map<Long, List<Occupation>> occupationByOrganisationId = findValidActiveUserOccupations(activeUser);

        final Function<HuntingClubArea, HuntingClub> areaToClubMapping = getAreaToClubMapping(list);
        final Function<HuntingClubArea, GISZoneWithoutGeometryDTO> areaToZoneMapping = createZoneDTOFunction(list);
        final Set<Long> zonesWithChanges = findChangedZonesForAreas(list);
        final List<Long> attachedAreas = listAreaWithAttachedGroup(list);

        final Map<Long, SystemUser> modifierUsers = findModifierUsers(list);
        final Function<SystemUser, Person> userToPerson = findUserToPerson(modifierUsers.values());

        return list.stream().map(area -> {
            final HuntingClub huntingClub = areaToClubMapping.apply(area);
            final GISZoneWithoutGeometryDTO zone = areaToZoneMapping.apply(area);

            final HuntingClubAreaDTO dto = HuntingClubAreaDTO.create(area, huntingClub, zone);

            dto.setLastModifiedDate(DateUtil.toLocalDateTimeNullSafe(area.getLifecycleFields().getModificationTime()));

            final Tuple2<String, Boolean> modifier = getModifier(area, modifierUsers, userToPerson);
            dto.setLastModifierName(modifier._1);
            dto.setLastModifierRiistakeskus(modifier._2);

            dto.setHasPendingZoneChanges(zonesWithChanges.contains(F.getId(zone)));
            dto.setAttachedToGroup(attachedAreas.contains(area.getId()));

            if (activeUser.isModeratorOrAdmin()) {
                dto.setCanEdit(true);
            } else {
                resolvePermissions(occupationByOrganisationId, area, dto);
            }

            return dto;
        }).collect(toList());
    }

    private static Tuple2<String, Boolean> getModifier(final HuntingClubArea area,
                                                       final Map<Long, SystemUser> modifierUsers,
                                                       final Function<SystemUser, Person> userToPerson) {

        final SystemUser user = modifierUsers.get(area.getModifiedByUserId());
        if (user != null) {
            final Person person = userToPerson.apply(user);
            if (person != null) {
                return Tuple.of(person.getFullName(), false);
            }
            return Tuple.of(user.getFullName(), true);
        }
        // user might be null for example when area is copied by scheduled task
        return Tuple.of(null, true);
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
                ? Collections.emptyMap()
                : F.groupByIdAfterTransform(findClubOccupations(person), Occupation::getOrganisation);
    }

    private List<Occupation> findClubOccupations(final Person person) {
        return occupationRepository.findActiveByPersonAndOrganisationTypes(person, EnumSet.of(OrganisationType.CLUB));
    }

    private static void resolvePermissions(final Map<Long, List<Occupation>> occupationByOrganisationId,
                                           final HuntingClubArea area,
                                           final HuntingClubAreaDTO dto) {
        final Long clubId = area.getClub() != null ? area.getClub().getId() : null;

        if (clubId == null || occupationByOrganisationId.isEmpty()) {
            return;
        }

        final List<Occupation> clubOccupations = occupationByOrganisationId.getOrDefault(clubId, Collections.emptyList());

        dto.setCanEdit(hasAnyOccupationWithType(clubOccupations, EnumSet.of(OccupationType.SEURAN_YHDYSHENKILO)));
    }

    private static boolean hasAnyOccupationWithType(final Collection<Occupation> occupations,
                                                    final EnumSet<OccupationType> occupationType) {
        return occupations.stream().anyMatch(o -> occupationType.contains(o.getOccupationType()));
    }

    private Set<Long> findChangedZonesForAreas(final Collection<HuntingClubArea> areas) {
        final Set<Long> zoneIds = getUniqueZoneIds(areas);

        return changedZoneCache.get().stream()
                .filter(zoneIds::contains)
                .collect(toSet());
    }

    private Map<Long, SystemUser> findModifierUsers(List<HuntingClubArea> list) {
        final Set<Long> modifierIds = F.mapNonNullsToSet(list, LifecycleEntity::getModifiedByUserId);

        return F.indexById(userRepository.findAll(modifierIds));
    }

    private Function<SystemUser, Person> findUserToPerson(Collection<SystemUser> users) {
        return CriteriaUtils.singleQueryFunction(users, SystemUser::getPerson, personRepository, false);
    }
}
