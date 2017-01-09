package fi.riista.feature.huntingclub.hunting.day;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.gamediary.observation.QObservation;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Repository
public class GroupHuntingDayRepositoryImpl implements GroupHuntingDayRepositoryCustom {

    @Resource
    private JPAQueryFactory jpaQueryFactory;

    @Override
    @Transactional
    public void deleteByHuntingClubGroup(final HuntingClubGroup group) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        jpaQueryFactory.delete(groupHuntingDay).where(groupHuntingDay.group.eq(group)).execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean groupHasHuntingDays(final HuntingClubGroup group) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        return 0 < jpaQueryFactory.selectFrom(groupHuntingDay).where(groupHuntingDay.group.eq(group)).fetchCount();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean clubHasMooseGroupsWithHuntingDays(final HuntingClub club) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QGameSpecies species = QGameSpecies.gameSpecies;

        final JPQLQuery<HuntingClubGroup> mooseGroups = JPAExpressions.selectFrom(group)
                .join(group.species, species)
                .where(group.parentOrganisation.eq(club),
                        species.officialCode.eq(GameSpecies.OFFICIAL_CODE_MOOSE));

        return 0 < jpaQueryFactory.selectFrom(groupHuntingDay)
                .where(groupHuntingDay.group.in(mooseGroups))
                .fetchCount();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean groupHasHarvestLinkedToHuntingDay(final HuntingClubGroup group) {
        final QHarvest harvest = QHarvest.harvest;
        return 0 < jpaQueryFactory
                .selectFrom(harvest)
                .where(harvest.huntingDayOfGroup.isNotNull(),
                        harvest.huntingDayOfGroup.in(queryGroupHuntingDays(group)))
                .fetchCount();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean groupHasObservationLinkedToHuntingDay(final HuntingClubGroup group) {
        final QObservation observation = QObservation.observation;
        return 0 < jpaQueryFactory
                .selectFrom(observation)
                .where(observation.huntingDayOfGroup.isNotNull(),
                        observation.huntingDayOfGroup.in(queryGroupHuntingDays(group)))
                .fetchCount();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean clubHasHarvestLinkedToHuntingDay(final HuntingClub club) {
        final QHarvest harvest = QHarvest.harvest;
        return 0 < jpaQueryFactory
                .selectFrom(harvest)
                .where(harvest.huntingDayOfGroup.isNotNull(),
                        harvest.huntingDayOfGroup.in(queryClubHuntingDays(club)))
                .fetchCount();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean clubHasObservationLinkedToHuntingDay(final HuntingClub club) {
        final QObservation observation = QObservation.observation;
        return 0 < jpaQueryFactory
                .selectFrom(observation)
                .where(observation.huntingDayOfGroup.isNotNull(),
                        observation.huntingDayOfGroup.in(queryClubHuntingDays(club)))
                .fetchCount();
    }

    private static JPQLQuery<GroupHuntingDay> queryGroupHuntingDays(final HuntingClubGroup group) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        return JPAExpressions.selectFrom(groupHuntingDay).where(groupHuntingDay.group.eq(group));
    }

    private static JPQLQuery<GroupHuntingDay> queryClubHuntingDays(final HuntingClub club) {
        final QGroupHuntingDay groupHuntingDay = QGroupHuntingDay.groupHuntingDay;
        return JPAExpressions.selectFrom(groupHuntingDay).where(groupHuntingDay.group.in(queryClubGroups(club)));
    }

    private static JPQLQuery<HuntingClubGroup> queryClubGroups(final HuntingClub club) {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return JPAExpressions.selectFrom(group).where(group.parentOrganisation.eq(club));
    }
}
