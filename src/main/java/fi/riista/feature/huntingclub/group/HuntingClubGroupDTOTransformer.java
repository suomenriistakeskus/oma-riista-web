package fi.riista.feature.huntingclub.group;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay_;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitService;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
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
import java.util.function.Function;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubGroupDTOTransformer extends ListTransformer<HuntingClubGroup, HuntingClubGroupDTO> {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private GameSpeciesRepository gameSpeciesRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingClubPermitService clubPermitService;

    @Nonnull
    @Override
    protected List<HuntingClubGroupDTO> transform(@Nonnull final List<HuntingClubGroup> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        final SystemUser activeUser = activeUserService.getActiveUser();
        final Map<Long, List<Occupation>> occupationByOrganisationId = findValidActiveUserOccupations(activeUser);

        final Function<HuntingClubGroup, GameSpecies> groupToSpeciesMapping = getGroupToSpeciesMapping(list);
        final Function<HuntingClubGroup, HarvestPermit> groupToPermitMapping = getGroupToPermitMapping(list);

        final Function<HuntingClubGroup, Long> huntingDayCountFn =
                CriteriaUtils.createAssociationCountFunction(list, GroupHuntingDay.class, GroupHuntingDay_.group, em);

        final Function<HuntingClubGroup, Long> memberCountFn = getGroupMemberCountMapping(list);

        final boolean adminOrModerator = activeUser.isModeratorOrAdmin();

        return list.stream().map(group -> {

            final GameSpecies gameSpecies = groupToSpeciesMapping.apply(group);
            final HarvestPermit permit = groupToPermitMapping.apply(group);

            final HuntingClubGroupDTO dto = HuntingClubGroupDTO.create(group, gameSpecies, permit);

            dto.setHuntingDaysExist(huntingDayCountFn.apply(group) > 0);
            dto.setMemberCount(memberCountFn.apply(group));

            dto.setHuntingFinished(clubPermitService.hasClubHuntingFinished(group));
            dto.setCanEdit(adminOrModerator || hasEditPermission(occupationByOrganisationId, group));

            return dto;
        }).collect(toList());
    }

    private Function<HuntingClubGroup, Long> getGroupMemberCountMapping(final Collection<HuntingClubGroup> groups) {
        final QOccupation occupation = QOccupation.occupation;

        final Map<Long, Long> countFn = new JPAQuery<>(em)
                .from(occupation)
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
            final List<Occupation> occupations = occupationRepository.findActiveByPersonAndOrganisationTypes(
                    activeUser.getPerson(), EnumSet.of(OrganisationType.CLUBGROUP, OrganisationType.CLUB));

            return F.groupByIdAfterTransform(occupations, Occupation::getOrganisation);
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
