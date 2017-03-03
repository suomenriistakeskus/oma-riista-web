package fi.riista.feature.huntingclub;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmountRepository;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class MoosePermitTodoFeature {

    @Resource
    private RequireEntityService entityService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private HarvestPermitSpeciesAmountRepository harvestPermitSpeciesAmountRepository;

    @Transactional(readOnly = true)
    public TodoDto listTodosForClub(long clubId, int year) {
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;

        final QHuntingClub club = QHuntingClub.huntingClub;
        final QHuntingClubArea area = QHuntingClubArea.huntingClubArea;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QOccupation occupation = QOccupation.occupation;

        // club has moose permits
        final boolean hasMoosePermits = queryFactory.from(permit)
                .join(permit.permitPartners, club).on(club.id.eq(clubId))
                .where(permit.permitTypeCode.eq(HarvestPermit.MOOSELIKE_PERMIT_TYPE))
                .fetchCount() > 0;

        if (!hasMoosePermits) {
            return new TodoDto(clubId, false, false, false, false);
        }
        // club has no active area
        final boolean noActiveArea = queryFactory.from(area)
                .where(area.club.id.eq(clubId), area.active.eq(true), area.huntingYear.eq(year))
                .fetchCount() == 0;

        // club has no group
        final boolean hasNoGroup = queryFactory.from(group)
                .join(group.parentOrganisation, club._super)
                .where(club.id.eq(clubId), group.huntingYear.eq(year))
                .fetchCount() == 0;


        // has group which is not linked to permit
        final boolean hasGroupNotLinkedToPermit = hasNoGroup || queryFactory.from(group)
                .join(group.parentOrganisation, club._super)
                .where(club.id.eq(clubId), group.huntingYear.eq(year), group.harvestPermit.isNull())
                .fetchCount() > 0;

        // has group which has no leader
        final JPQLQuery<Long> groupIdsHavingLeader = JPAExpressions.select(occupation.organisation.id)
                .from(occupation)
                .join(occupation.organisation, group._super)
                .join(group.parentOrganisation, club._super)
                .where(club.id.eq(clubId),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        occupation.validAndNotDeleted());
        final boolean hasGroupsWithoutLeader = hasNoGroup || queryFactory.from(group)
                .join(group.parentOrganisation, club._super)
                .where(club.id.eq(clubId),
                        group.huntingYear.eq(year),
                        group.id.notIn(groupIdsHavingLeader))
                .fetchCount() > 0;

        return new TodoDto(clubId, noActiveArea, hasNoGroup, hasGroupNotLinkedToPermit, hasGroupsWithoutLeader);
    }

    @Transactional(readOnly = true)
    public Map<Long, TodoDto> listTodos(final long permitId, final int speciesCode) {
        final int year = findHuntingYear(entityService.requireHarvestPermit(permitId, EntityPermission.READ), speciesCode)
                .orElseThrow(() -> new IllegalStateException("Could not find huntingYear"));

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
                .where(area.active.eq(true), area.huntingYear.eq(year));
        final List<Long> hasArea = baseQuery(permitId, speciesCode, permit, hpsa, species, club)
                .where(club.id.in(areaActiveAndYearMatches))
                .fetch();

        final BooleanExpression groupSpeciesAndYearMatches = group.species.eq(species).and(group.huntingYear.eq(year));

        // club has group for species
        final List<Long> hasGroup = baseQuery(permitId, speciesCode, permit, hpsa, species, club)
                .join(club.subOrganisations, group._super)
                .where(groupSpeciesAndYearMatches)
                .fetch();

        // club has group for species which is linked to permit
        final List<Long> hasGroupLinkedToPermit = baseQuery(permitId, speciesCode, permit, hpsa, species, club)
                .join(club.subOrganisations, group._super)
                .where(groupSpeciesAndYearMatches, group.harvestPermit.eq(permit))
                .fetch();

        // club has group for species which has hunting leader
        final JPQLQuery<Long> groupIdsHavingLeader = JPAExpressions.select(occupation.organisation.id)
                .from(occupation)
                .where(occupation.organisation.eq(group._super),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        occupation.validAndNotDeleted());

        final List<Long> hasGroupHuntingLeader = baseQuery(permitId, speciesCode, permit, hpsa, species, club)
                .join(club.subOrganisations, group._super)
                .where(groupSpeciesAndYearMatches, group.id.in(groupIdsHavingLeader))
                .fetch();

        final List<Long> partners = baseQuery(permitId, speciesCode, permit, hpsa, species, club)
                .distinct()
                .fetch();
        return partners.stream()
                .map(id -> new TodoDto(id,
                        !hasArea.contains(id),
                        !hasGroup.contains(id),
                        !hasGroupLinkedToPermit.contains(id),
                        !hasGroupHuntingLeader.contains(id)
                ))
                .collect(toMap(TodoDto::getClubId, Function.identity()));
    }

    private OptionalInt findHuntingYear(HarvestPermit p, int speciesCode) {
        return harvestPermitSpeciesAmountRepository.findOneByHarvestPermitIdAndSpeciesCode(p.getId(), speciesCode)
                .map(Has2BeginEndDates::findUnambiguousHuntingYear)
                .orElseGet(OptionalInt::empty);
    }

    private JPQLQuery<Long> baseQuery(final long permitId,
                                      final int speciesCode,
                                      final QHarvestPermit permit,
                                      final QHarvestPermitSpeciesAmount hpsa,
                                      final QGameSpecies species,
                                      final QHuntingClub club) {

        return queryFactory.from(permit)
                .join(permit.speciesAmounts, hpsa)
                .join(hpsa.gameSpecies, species)
                .join(permit.permitPartners, club)
                .where(permit.id.eq(permitId), species.officialCode.eq(speciesCode))
                .select(club.id);
    }

    public static class TodoDto {
        @JsonIgnore
        private final long clubId;
        private final boolean areaMissing;
        private final boolean groupMissing;
        private final boolean groupPermitMissing;
        private final boolean groupLeaderMissing;

        public TodoDto(final long clubId,
                       final boolean areaMissing,
                       final boolean groupMissing,
                       final boolean groupPermitMissing,
                       final boolean groupLeaderMissing) {

            this.clubId = clubId;
            this.areaMissing = areaMissing;
            this.groupMissing = groupMissing;
            this.groupPermitMissing = groupPermitMissing;
            this.groupLeaderMissing = groupLeaderMissing;
        }

        public long getClubId() {
            return clubId;
        }

        public boolean isAreaMissing() {
            return areaMissing;
        }

        public boolean isGroupMissing() {
            return groupMissing;
        }

        public boolean isGroupPermitMissing() {
            return groupPermitMissing;
        }

        public boolean isGroupLeaderMissing() {
            return groupLeaderMissing;
        }

        @JsonGetter
        public boolean isTodo() {
            return areaMissing || groupMissing || groupPermitMissing || groupLeaderMissing;
        }
    }

}
