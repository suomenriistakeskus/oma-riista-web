package fi.riista.feature.huntingclub.group;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.pilot.DeerPilotRepository;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.feature.organization.OrganisationType.CLUB;
import static fi.riista.feature.organization.OrganisationType.CLUBGROUP;
import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.util.Collect.groupingByIdOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubGroupDTOTransformer extends ListTransformer<HuntingClubGroup, HuntingClubGroupDTO> {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private DeerPilotRepository deerPilotRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Nonnull
    @Override
    protected List<HuntingClubGroupDTO> transform(@Nonnull final List<HuntingClubGroup> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        final SystemUser activeUser = activeUserService.requireActiveUser();
        final Map<Long, List<Occupation>> occupationByOrganisationId = findValidActiveUserOccupations(activeUser);

        final Function<HuntingClubGroup, GameSpecies> groupToSpeciesMapping = getGroupToSpeciesMapping(list);
        final Function<HuntingClubGroup, HarvestPermit> groupToPermitMapping = getGroupToPermitMapping(list);

        final List<Long> idsOfGroupsHavingHuntingDays = getGroupIdsHavingHuntingDays(list);

        final Function<HuntingClubGroup, Long> memberCountFn = getGroupMemberCountMapping(list);
        final List<HuntingClubGroup> groupsInPilot = deerPilotRepository.filterGroupsInPilot(list);
        final boolean adminOrModerator = activeUser.isModeratorOrAdmin();

        return list.stream().map(group -> {

            final GameSpecies gameSpecies = groupToSpeciesMapping.apply(group);
            final HarvestPermit permit = groupToPermitMapping.apply(group);

            final HuntingClubGroupDTO dto =
                    HuntingClubGroupDTO.create(group, gameSpecies, permit, groupsInPilot.contains(group));

            dto.setHuntingDaysExist(idsOfGroupsHavingHuntingDays.contains(group.getId()));
            dto.setMemberCount(memberCountFn.apply(group));

            dto.setHuntingFinished(huntingFinishingService.hasPermitPartnerFinishedHunting(group));
            dto.setCanEdit(!group.isFromMooseDataCard() &&
                    (adminOrModerator ||
                            hasEditPermission(occupationByOrganisationId, group)
                                    && !harvestPermitLockedByDateService.isPermitLocked(group)));

            return dto;
        }).collect(toList());
    }

    private List<Long> getGroupIdsHavingHuntingDays(@Nonnull final List<HuntingClubGroup> list) {
        final Set<Long> groupIds = F.getUniqueIds(list);

        final QHarvest harvest = QHarvest.harvest;
        final QGroupHuntingDay huntingDay = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        final List<Long> groupsHavingDeerHarvests = queryFactory.select(group.id)
                .from(group)
                .join(group.species, species)
                .join(group.huntingDays, huntingDay)
                .join(huntingDay.harvests, harvest)
                .where(group.id.in(groupIds),
                        species.officialCode.in(GameSpecies.DEER_CODES_REQUIRING_PERMIT_FOR_HUNTING)
                )
                .distinct()
                .fetch();

        final List<Long> groupsHavingMooseHuntingDays = queryFactory.select(group.id)
                .from(group)
                .join(group.species, species)
                .join(group.huntingDays, huntingDay)
                .where(group.id.in(groupIds),
                        species.officialCode.eq(GameSpecies.OFFICIAL_CODE_MOOSE)
                )
                .distinct()
                .fetch();
        return F.concat(groupsHavingDeerHarvests, groupsHavingMooseHuntingDays);
    }

    private Function<HuntingClubGroup, Long> getGroupMemberCountMapping(final Collection<HuntingClubGroup> groups) {
        final QOccupation occupation = QOccupation.occupation;

        final Map<Long, Long> countFn = queryFactory.from(occupation)
                .select(occupation.person.countDistinct())
                .where(occupation.organisation.in(groups).and(occupation.validAndNotDeleted()))
                .groupBy(occupation.organisation)
                .transform(GroupBy.groupBy(occupation.organisation.id).as(occupation.person.countDistinct()));

        return t -> countFn.getOrDefault(F.getId(t), 0L);
    }

    private Function<HuntingClubGroup, GameSpecies> getGroupToSpeciesMapping(final Iterable<HuntingClubGroup> groups) {
        return CriteriaUtils.singleQueryFunction(groups, HuntingClubGroup::getSpecies, gameSpeciesRepository, true);
    }

    private Function<HuntingClubGroup, HarvestPermit> getGroupToPermitMapping(final Iterable<HuntingClubGroup> groups) {
        return CriteriaUtils.singleQueryFunction(
                groups, HuntingClubGroup::getHarvestPermit, harvestPermitRepository, false);
    }

    private Map<Long, List<Occupation>> findValidActiveUserOccupations(final SystemUser activeUser) {
        if (activeUser.getRole() == SystemUser.Role.ROLE_USER && activeUser.getPerson() != null) {
            return occupationRepository
                    .findActiveByPersonAndOrganisationTypes(activeUser.getPerson(), EnumSet.of(CLUBGROUP, CLUB))
                    .stream()
                    .collect(groupingByIdOf(Occupation::getOrganisation));
        }

        return Collections.emptyMap();
    }

    private static boolean hasEditPermission(final Map<Long, List<Occupation>> occupationByOrganisationId,
                                             final HuntingClubGroup group) {

        final Long clubId = F.getId(group.getParentOrganisation());

        if (clubId == null || occupationByOrganisationId.isEmpty() || group.isFromMooseDataCard()) {
            return false;
        }

        final List<Occupation> allOccupations = F.concat(
                occupationByOrganisationId.getOrDefault(group.getId(), emptyList()),
                occupationByOrganisationId.getOrDefault(clubId, emptyList()));

        return hasAnyOccupationWithType(allOccupations, EnumSet.of(SEURAN_YHDYSHENKILO, RYHMAN_METSASTYKSENJOHTAJA));
    }

    private static boolean hasAnyOccupationWithType(final Collection<Occupation> occupations,
                                                    final EnumSet<OccupationType> occupationType) {

        return occupations.stream().map(Occupation::getOccupationType).anyMatch(occupationType::contains);
    }
}
