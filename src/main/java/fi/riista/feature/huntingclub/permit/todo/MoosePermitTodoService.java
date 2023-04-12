package fi.riista.feature.huntingclub.permit.todo;

import static fi.riista.util.Collect.indexingBy;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.permit.statistics.ClubHuntingSummaryBasicInfoService;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.permit.PermitTypeCode;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoosePermitTodoService {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Resource
    private ClubHuntingSummaryBasicInfoService clubHuntingSummaryBasicInfoService;

    @Transactional(readOnly = true)
    public MoosePermitTodoDTO listTodosForClub(final HuntingClub huntingClub, final int year) {
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount speciesAmount = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies gameSpecies = QGameSpecies.gameSpecies;

        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHuntingClubArea area = QHuntingClubArea.huntingClubArea;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QOccupation occupation = QOccupation.occupation;

        // club has moose permits
        final boolean hasMoosePermits = queryFactory.from(permit)
                .join(permit.permitPartners, club).on(club.eq(huntingClub))
                .where(permit.permitTypeCode.eq(PermitTypeCode.MOOSELIKE))
                .fetchCount() > 0;

        if (!hasMoosePermits) {
            return MoosePermitTodoDTO.noTodo(huntingClub.getId());
        }
        // club has no active area
        final boolean noActiveArea = queryFactory.from(area)
                .where(area.club.eq(huntingClub), area.active.eq(true), area.huntingYear.eq(year))
                .fetchCount() == 0;

        // club has no group
        final boolean hasNoGroup = queryFactory.from(group)
                .join(group.parentOrganisation, club._super)
                .where(club.eq(huntingClub), group.huntingYear.eq(year))
                .fetchCount() == 0;


        // has group which is not linked to permit
        final boolean hasGroupNotLinkedToPermit = hasNoGroup || queryFactory.from(group)
                .join(group.parentOrganisation, club._super)
                .where(club.eq(huntingClub), group.huntingYear.eq(year), group.harvestPermit.isNull())
                .fetchCount() > 0;

        // has group which has no leader
        final JPQLQuery<Long> groupIdsHavingLeader = JPAExpressions.select(occupation.organisation.id)
                .from(occupation)
                .join(occupation.organisation, group._super)
                .join(group.parentOrganisation, club._super)
                .where(club.eq(huntingClub),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        occupation.validAndNotDeleted());

        final boolean hasGroupsWithoutLeader = hasNoGroup || queryFactory.from(group)
                .join(group.parentOrganisation, club._super)
                .where(club.eq(huntingClub),
                        group.huntingYear.eq(year),
                        group.id.notIn(groupIdsHavingLeader))
                .fetchCount() > 0;

        // mooselike speciesAmounts hunting is passed, but no summary sent
        final List<HarvestPermitSpeciesAmount> spas = queryFactory
                .select(speciesAmount)
                .from(permit)
                .join(permit.permitPartners, club).on(club.eq(huntingClub))
                .join(permit.speciesAmounts, speciesAmount).on(speciesAmount.validOnHuntingYear(year).and(speciesAmount.validityPassed()))
                .join(speciesAmount.gameSpecies, gameSpecies)
                .where(permit.permitTypeCode.eq(PermitTypeCode.MOOSELIKE))
                .fetch();

        final boolean partnerHuntingSummaryMissing = !spas.isEmpty() && spas.stream().anyMatch(spa -> !clubHuntingSummaryBasicInfoService
                .getHuntingSummariesGroupedByClubId(spa.getHarvestPermit(), spa.getGameSpecies().getOfficialCode())
                .get(huntingClub.getId()).isHuntingFinished());



        return new MoosePermitTodoDTO(huntingClub.getId(), noActiveArea, hasNoGroup, hasGroupNotLinkedToPermit, hasGroupsWithoutLeader, partnerHuntingSummaryMissing);
    }

    @Transactional(readOnly = true)
    public Map<Long, MoosePermitTodoDTO> listTodos(final HarvestPermit harvestPermit, final GameSpecies gameSpecies) {
        final int huntingYear = harvestPermitSpeciesAmountRepository
                .getOneByHarvestPermitAndSpeciesCode(harvestPermit, gameSpecies.getOfficialCode())
                .resolveHuntingYear();

        final QHarvestPermit permit = QHarvestPermit.harvestPermit;
        final QHarvestPermitSpeciesAmount hpsa = QHarvestPermitSpeciesAmount.harvestPermitSpeciesAmount;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHuntingClubArea area = QHuntingClubArea.huntingClubArea;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QOccupation occupation = QOccupation.occupation;

        // club has active area
        final JPQLQuery<Long> areaActiveAndYearMatches = JPAExpressions.select(area.club.id)
                .from(area)
                .where(area.active.eq(true), area.huntingYear.eq(huntingYear));
        final List<Long> hasArea = baseQuery(harvestPermit, gameSpecies, permit, hpsa, species, club)
                .where(club.id.in(areaActiveAndYearMatches))
                .fetch();

        final BooleanExpression groupSpeciesAndYearMatches = group.species.eq(species).and(group.huntingYear.eq(huntingYear));

        // club has group for species
        final List<Long> hasGroup = baseQuery(harvestPermit, gameSpecies, permit, hpsa, species, club)
                .join(club.subOrganisations, group._super)
                .where(groupSpeciesAndYearMatches)
                .fetch();

        // club has group for species which is linked to permit
        final List<Long> hasGroupLinkedToPermit = baseQuery(harvestPermit, gameSpecies, permit, hpsa, species, club)
                .join(club.subOrganisations, group._super)
                .where(groupSpeciesAndYearMatches, group.harvestPermit.eq(permit))
                .fetch();

        // club has group for species which has hunting leader
        final JPQLQuery<Long> groupIdsHavingLeader = JPAExpressions.select(occupation.organisation.id)
                .from(occupation)
                .where(occupation.organisation.eq(group._super),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        occupation.validAndNotDeleted());

        final List<Long> hasGroupHuntingLeader = baseQuery(harvestPermit, gameSpecies, permit, hpsa, species, club)
                .join(club.subOrganisations, group._super)
                .where(groupSpeciesAndYearMatches, group.id.in(groupIdsHavingLeader))
                .fetch();

        return baseQuery(harvestPermit, gameSpecies, permit, hpsa, species, club)
                .distinct()
                .fetch()
                .stream()
                .map(id -> new MoosePermitTodoDTO(id,
                        !hasArea.contains(id),
                        !hasGroup.contains(id),
                        !hasGroupLinkedToPermit.contains(id),
                        !hasGroupHuntingLeader.contains(id),
                        false
                ))
                .collect(indexingBy(MoosePermitTodoDTO::getClubId));
    }

    private JPQLQuery<Long> baseQuery(final HarvestPermit harvestPermit,
                                      final GameSpecies gameSpecies,
                                      final QHarvestPermit permit,
                                      final QHarvestPermitSpeciesAmount hpsa,
                                      final QGameSpecies species,
                                      final QHuntingClub club) {

        return queryFactory.from(permit)
                .join(permit.speciesAmounts, hpsa)
                .join(hpsa.gameSpecies, species)
                .join(permit.permitPartners, club)
                .where(permit.eq(harvestPermit), species.eq(gameSpecies))
                .select(club.id);
    }

}
