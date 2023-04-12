package fi.riista.feature.gamediary.harvest;

import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.season.HarvestArea;
import fi.riista.feature.harvestpermit.season.QHarvestArea;
import fi.riista.feature.harvestpermit.season.QHarvestQuota;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.QGroupHuntingDay;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.sql.SQGameSpecies;
import fi.riista.sql.SQGroupHarvestRejection;
import fi.riista.sql.SQGroupHuntingDay;
import fi.riista.sql.SQHarvest;
import fi.riista.sql.SQHuntingClubArea;
import fi.riista.sql.SQOccupation;
import fi.riista.sql.SQOrganisation;
import fi.riista.sql.SQPerson;
import fi.riista.sql.SQRhy;
import fi.riista.sql.SQZone;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import org.geolatte.geom.Geometry;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.sql.SQLExpressions.union;
import static fi.riista.sql.SQGroupHarvestRejection.groupHarvestRejection;
import static java.util.Arrays.asList;

@Transactional
@Repository
public class HarvestRepositoryImpl implements HarvestRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates sqlTemplates;

    @Resource
    private SQLQueryFactory sqlQueryFactory;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    /**
     * Filter criteria includes:
     * 1) Person must have occupation in ClubGroup
     * 2) Person must have occupation in Club (to ignore invitations)
     * 2) Harvest location must intersect with defined area for ClubGroup
     * OR harvest must be linked to GroupHuntingDay
     * 3) OR Harvest is rejected by the group
     */
    @Override
    @Transactional(readOnly = true)
    public List<Harvest> findGroupHarvest(final HuntingClubGroup huntingClubGroup, final Interval interval) {

        final SQHarvest harvest = new SQHarvest("harvest");
        final QHarvest harvestEntity = new QHarvest("harvest");
        final SQGroupHarvestRejection rejection = groupHarvestRejection;

        final SubQueryExpression<Long> subQuery1 =
                harvestForGroupMemberInsideGroupHuntingArea(huntingClubGroup.getId(), interval);

        final SubQueryExpression<Long> subQuery2 =
                harvestLinkedToGroupHuntingDay(huntingClubGroup.getId());

        final BooleanExpression rejected = SQLExpressions.selectOne().from(rejection)
                .where(rejection.huntingClubGroupId.eq(huntingClubGroup.getId())
                        .and(rejection.harvestId.eq(harvest.harvestId)))
                .exists();

        return new JPASQLQuery<Harvest>(entityManager, sqlTemplates)
                .from(harvest).select(harvestEntity)
                .where(harvest.harvestId.in(union(asList(subQuery1, subQuery2))).or(rejected))
                .orderBy(harvest.pointOfTime.desc(), harvest.harvestId.desc())
                .fetch();
    }

    private static SubQueryExpression<Long> harvestForGroupMemberInsideGroupHuntingArea(final long huntingGroupId,
                                                                                        final Interval interval) {
        /*
        SELECT sq.harvest_id FROM (
        SELECT DISTINCT a.zone_id, h2.harvest_id, h2.geom as geom
        FROM occupation groupOcc
        INNER JOIN organisation g ON (groupOcc.deletion_time IS NULL AND g.organisation_id = groupOcc.organisation_id AND g.organisation_type = 'CLUBGROUP')
        INNER JOIN occupation clubOcc ON (clubOcc.deletion_time IS NULL AND clubOcc.organisation_id = g.parent_organisation_id AND clubOcc.person_id = groupOcc.person_id)
        INNER JOIN hunting_club_area a ON (a.hunting_club_area_id = g.hunting_area_id)
        INNER JOIN zone z ON (z.zone_id = a.zone_id)
        INNER JOIN person p ON (p.person_id = groupOcc.person_id)
        INNER JOIN harvest h2 ON p.person_id IN (h2.author_id, h2.actual_shooter_id)
        WHERE groupOcc.organisation_id = :huntingGroupId
        AND h2.point_of_time BETWEEN COALESCE(groupOcc.begin_date, h2.point_of_time) AND COALESCE(groupOcc.end_date, h2.point_of_time)
        AND h2.point_of_time BETWEEN COALESCE(clubOcc.begin_date, h2.point_of_time) AND COALESCE(clubOcc.end_date, h2.point_of_time)
        AND h2.group_hunting_day_id IS NULL
        AND h2.harvest_report_state IS NULL
        AND h2.harvest_permit_id IS NULL
        AND h2.game_species_id = g.game_species_id
        AND h2.point_of_time >= :beginTime AND h2.point_of_time < :endTime
        ) sq JOIN zone z ON z.zone_id = sq.zone_id
        AND ST_Intersects(z.geom, sq.geom)
        */
        final SQHarvest harvest = new SQHarvest("h2");
        final SQOrganisation group = new SQOrganisation("g");
        final SQOccupation groupOccupation = new SQOccupation("groupOcc");
        final SQOccupation clubOccupation = new SQOccupation("clubOcc");
        final SQHuntingClubArea huntingClubArea = new SQHuntingClubArea("a");
        final SQZone zone = new SQZone("z");
        final SQPerson person = new SQPerson("p");

        final DateExpression<java.sql.Date> harvestDate = SQLExpressions.date(java.sql.Date.class, harvest.pointOfTime);

        final SQLQuery<Tuple> subQuery = SQLExpressions
                .select(huntingClubArea.zoneId, harvest.harvestId, harvest.geom.as("geom"))
                .distinct()
                .from(groupOccupation)
                .join(group).on(group.organisationType.eq(OrganisationType.CLUBGROUP.name())
                        .and(groupOccupation.organisationId.eq(group.organisationId)
                                .and(groupOccupation.deletionTime.isNull())))
                .join(clubOccupation).on(clubOccupation.organisationId.eq(group.parentOrganisationId)
                        .and(clubOccupation.personId.eq(groupOccupation.personId))
                        .and(clubOccupation.deletionTime.isNull()))
                .join(huntingClubArea).on(huntingClubArea.huntingClubAreaId.eq(group.huntingAreaId))
                .join(person).on(person.personId.eq(groupOccupation.personId))
                .join(harvest).on(person.personId.eq(harvest.authorId).or(person.personId.eq(harvest.actualShooterId)))
                .where(groupOccupation.organisationId.eq(huntingGroupId)
                        .and(harvestDate.between(
                                groupOccupation.beginDate.coalesce(harvestDate),
                                groupOccupation.endDate.coalesce(harvestDate)))
                        .and(harvestDate.between(
                                clubOccupation.beginDate.coalesce(harvestDate),
                                clubOccupation.endDate.coalesce(harvestDate)))
                        .and(harvest.groupHuntingDayId.isNull())
                        .and(harvest.harvestReportState.isNull())
                        .and(harvest.harvestPermitId.isNull())
                        .and(harvest.gameSpeciesId.eq(group.gameSpeciesId))
                        .and(harvest.pointOfTime.between(
                                new Timestamp(interval.getStartMillis()),
                                new Timestamp(interval.getEndMillis())
                        )));

        final PathBuilder<Object[]> sq = new PathBuilder<>(Object[].class, "sq");
        final PathBuilder<Geometry> sqGeomPath = sq.get("geom", Geometry.class);
        final GeometryExpression<Geometry> sqGeom = GeometryExpressions.asGeometry(sqGeomPath);

        return SQLExpressions
                .select(sq.get("harvest_id", Long.class))
                .from(subQuery.as("sq"))
                .join(zone).on(zone.zoneId.eq(sq.get("zone_id", Long.class)))
                .where(zone.geom.intersects(sqGeom));
    }

    private static SubQueryExpression<Long> harvestLinkedToGroupHuntingDay(final long huntingGroupId) {
        /*
        SELECT h3.harvest_id
        FROM harvest h3
        WHERE h3.group_hunting_day_id IN (
          SELECT group_hunting_day_id
          FROM group_hunting_day
          WHERE hunting_group_id = :huntingGroupId)
        */
        final SQHarvest harvest = new SQHarvest("h3");
        final SQGroupHuntingDay groupHuntingDay = SQGroupHuntingDay.groupHuntingDay;

        final SQLQuery<Long> groupHuntingDayIds = SQLExpressions.selectOne()
                .from(groupHuntingDay)
                .where(groupHuntingDay.huntingGroupId.eq(huntingGroupId))
                .select(groupHuntingDay.groupHuntingDayId);

        return SQLExpressions.select(harvest.harvestId)
                .from(harvest)
                .where(harvest.groupHuntingDayId.in(groupHuntingDayIds));
    }

    /**
     * Filter criteria includes:
     * - If harvest is linked to hunting club, it is included
     * Else:
     * - If harvest is linked to groups hunting day, it is included always
     * Otherwise:
     * - Person must have occupation in given club
     * - Occupation must be valid on the day of harvest
     * - If harvest.harvest_report_required=true then harvest must have accepted harvest report
     * - Harvest species must not be any the given mooselikes (those are included by hunting days)
     * - Harvest location must intersect with active area for club
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> countClubHarvestAmountGroupByGameSpeciesId(final HuntingClub huntingClub,
                                                                         final int huntingYear,
                                                                         final Interval interval,
                                                                         final Set<Integer> mooselike) {
        final SQHarvest harvest = SQHarvest.harvest;

        final BooleanExpression huntingClubSelectedPredicate = harvest.huntingClubId.isNotNull()
                .and(harvest.huntingClubId.eq(huntingClub.getId()))
                .and(harvest.pointOfTime.goe(new Timestamp(interval.getStartMillis())))
                .and(harvest.pointOfTime.lt(new Timestamp(interval.getEndMillis())));;

        final BooleanExpression huntingAreaPredicate = harvest.huntingClubId.isNull()
                .and(harvest.groupHuntingDayId.isNull())
                .and(harvest.harvestId.in(harvestOfClubMemberInsideClubHuntingArea(
                        huntingClub.getId(), interval, huntingYear, mooselike)));

        final BooleanExpression huntingDayPredicate = harvest.huntingClubId.isNull()
                .and(harvest.groupHuntingDayId.isNotNull())
                .and(harvest.groupHuntingDayId.in(harvestLinkedToClubHuntingDay(huntingClub.getId(), huntingYear)))
                .and(harvest.pointOfTime.goe(new Timestamp(interval.getStartMillis())))
                .and(harvest.pointOfTime.lt(new Timestamp(interval.getEndMillis())));

        final NumberPath<Long> keyPath = harvest.gameSpeciesId;
        final NumberExpression<Integer> valuePath = harvest.amount.sum();

        return sqlQueryFactory.from(harvest)
                .select(keyPath, valuePath)
                .where(huntingClubSelectedPredicate.or(huntingAreaPredicate).or(huntingDayPredicate))
                .groupBy(keyPath)
                .transform(GroupBy.groupBy(keyPath).as(valuePath));
    }

    private static SQLQuery<Long> harvestOfClubMemberInsideClubHuntingArea(final Long huntingClubId,
                                                                           final Interval interval,
                                                                           final int huntingYear,
                                                                           final Set<Integer> mooseLikeOfficialCodes) {

        final SQOccupation clubOccupation = new SQOccupation("occ");
        final SQPerson person = new SQPerson("p");
        final SQHarvest harvest = new SQHarvest("h2");
        final SQHuntingClubArea huntingClubArea = new SQHuntingClubArea("hca");
        final SQGameSpecies gameSpecies = SQGameSpecies.gameSpecies;
        final SQZone zone = new SQZone("z");

        final BooleanExpression isAuthorOrShooter = harvest.authorId.eq(person.personId)
                .or(harvest.actualShooterId.eq(person.personId));

        final DateExpression<Date> harvestDate = SQLExpressions.date(Date.class, harvest.pointOfTime);

        final SQLQuery<Long> queryMooseLikeSpeciesIds = SQLExpressions
                .select(gameSpecies.gameSpeciesId)
                .from(gameSpecies)
                .where(gameSpecies.officialCode.in(mooseLikeOfficialCodes));

        final SQLQuery<Long> memberHarvests = SQLExpressions
                .select(harvest.harvestId)
                .from(clubOccupation)
                .join(person).on(clubOccupation.organisationId.eq(huntingClubId)
                        .and(clubOccupation.personId.eq(person.personId)))
                .join(harvest).on(isAuthorOrShooter.and(harvestDate.between(
                        clubOccupation.beginDate.coalesce(harvestDate),
                        clubOccupation.endDate.coalesce(harvestDate))))
                .where(clubOccupation.deletionTime.isNull())
                .where(harvest.pointOfTime.goe(new Timestamp(interval.getStartMillis())))
                .where(harvest.pointOfTime.lt(new Timestamp(interval.getEndMillis())))
                .where(harvest.harvestReportRequired.isFalse().or(harvest.harvestReportRequired.isTrue()
                        .and(harvest.harvestReportState.eq(HarvestReportState.APPROVED.name()))))
                .where(harvest.gameSpeciesId.notIn(queryMooseLikeSpeciesIds));

        // Match harvests from members with club area
        return SQLExpressions.select(harvest.harvestId)
                .from(huntingClubArea)
                .join(zone).on(huntingClubArea.zoneId.eq(zone.zoneId))
                .join(harvest).on((zone.geom.intersects(harvest.geom)))
                .where(huntingClubArea.clubId.eq(huntingClubId))
                .where(huntingClubArea.huntingYear.eq(huntingYear))
                .where(huntingClubArea.isActive.isTrue())
                .where(harvest.harvestId.in(memberHarvests));

    }

    private static SQLQuery<Long> harvestLinkedToClubHuntingDay(final long huntingClubId,
                                                                final int huntingYear) {
        final SQGroupHuntingDay groupHuntingDay = SQGroupHuntingDay.groupHuntingDay;
        final SQOrganisation group = new SQOrganisation("group");

        return SQLExpressions
                .select(groupHuntingDay.groupHuntingDayId)
                .from(groupHuntingDay)
                .join(group).on(group.parentOrganisationId.eq(huntingClubId)
                        .and(groupHuntingDay.huntingGroupId.eq(group.organisationId)))
                .where(group.huntingYear.eq(huntingYear));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Harvest> findHarvestsLinkedToHuntingDayWithinAreaOfRhy(final Riistanhoitoyhdistys rhy,
                                                                       final GameSpecies species,
                                                                       final Interval interval) {

        final SubQueryExpression<Long> rhyGeometryQuery =
                findHarvestsLinkedToHuntingDayIntersectingRiistanhoitoyhdistysGeometry(
                        rhy.getOfficialCode(), species, interval);

        final SQHarvest harvest = SQHarvest.harvest;

        return new JPASQLQuery<>(entityManager, sqlTemplates)
                .select(QHarvest.harvest)
                .from(harvest)
                .where(harvest.harvestId.in(rhyGeometryQuery))
                .fetch();
    }

    /* Linked to hunting day and on RHY area

    select h2.harvest_id
    from rhy
    join harvest h2 on rhy.geom && h2.geom
    where rhy.id = '459'
    and h2.game_species_id = (select game_species_id from game_species where official_code = 47503)
    and h2.group_hunting_day_id is not null;
     */
    private static SubQueryExpression<Long> findHarvestsLinkedToHuntingDayIntersectingRiistanhoitoyhdistysGeometry(
            final String rhyOfficialCode,
            final GameSpecies species,
            final Interval interval) {

        final SQHarvest h2 = new SQHarvest("h2");
        final SQRhy rhy = SQRhy.rhy;

        final BooleanExpression predicate = rhy.id.eq(rhyOfficialCode)
                .and(h2.groupHuntingDayId.isNotNull())
                .and(h2.pointOfTime.between(
                        new Timestamp(interval.getStart().getMillis()),
                        new Timestamp(interval.getEnd().getMillis())))
                .and(h2.gameSpeciesId.eq(species.getId()));

        return SQLExpressions.select(h2.harvestId)
                .from(rhy)
                .join(h2).on(rhy.geom.intersects(h2.geom.transform(GISUtils.SRID.ETRS_TM35.value)))
                .where(predicate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Harvest> findHarvestsLinkedToHuntingDayAndPermitOfRhy(final Riistanhoitoyhdistys rhy,
                                                                      final GameSpecies species,
                                                                      final Interval interval) {

        final QHarvest harvest = QHarvest.harvest;
        final QGroupHuntingDay huntingDay = QGroupHuntingDay.groupHuntingDay;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QHarvestPermit permit = QHarvestPermit.harvestPermit;

        return new JPAQuery<>(entityManager)
                .from(harvest)
                .join(harvest.huntingDayOfGroup, huntingDay)
                .join(huntingDay.group, group)
                .join(group.harvestPermit, permit)
                .select(harvest)
                .where(permit.rhy.eq(rhy)
                        .and(harvest.species.eq(species))
                        .and(harvest.pointOfTime.between(
                                interval.getStart(),
                                interval.getEnd())))
                .fetch();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<Long, Integer> countByHarvestPermitIdAndSpeciesCode(final Collection<Long> permits, final int speciesCode) {
        if (permits.isEmpty()) {
            return Collections.emptyMap();
        }

        final QHarvest HARVEST = QHarvest.harvest;
        final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;

        return jpqlQueryFactory.selectFrom(HARVEST)
                .innerJoin(HARVEST.harvestPermit, PERMIT)
                .innerJoin(HARVEST.species, SPECIES)
                .where(PERMIT.id.in(permits)
                        .and(SPECIES.officialCode.eq(speciesCode))
                        .and(HARVEST.harvestReportState.eq(HarvestReportState.APPROVED)))
                .transform(GroupBy.groupBy(PERMIT.id).as(GroupBy.sum(HARVEST.amount)));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<HarvestArea.HarvestAreaDetailedType, Integer> countQuotaHarvestsByArea(final int speciesCode, final int huntingYear) {
        final QHarvest HARVEST = QHarvest.harvest;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvestQuota QUOTA = QHarvestQuota.harvestQuota;
        final QHarvestArea AREA = QHarvestArea.harvestArea;

        final DateTime huntingYearStart = DateUtil.toDateTimeNullSafe(DateUtil.huntingYearBeginDate(huntingYear));
        final DateTime huntingYearEnd = DateUtil.toDateTimeNullSafe(DateUtil.huntingYearEndDate(huntingYear)).plusDays(1).minusMillis(1);

        return jpqlQueryFactory
                .select(AREA.nameFinnish, HARVEST.amount.sum())
                .from(HARVEST)
                .innerJoin(HARVEST.species, SPECIES)
                .innerJoin(HARVEST.harvestQuota, QUOTA)
                .innerJoin(QUOTA.harvestArea, AREA)
                .where(SPECIES.officialCode.eq(speciesCode)
                        .and(HARVEST.pointOfTime.between(huntingYearStart, huntingYearEnd))
                        .and(HARVEST.harvestReportState.eq(HarvestReportState.APPROVED)))
                .groupBy(AREA.nameFinnish)
                .transform(GroupBy.groupBy(AREA.nameFinnish).as(HARVEST.amount.sum()))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> HarvestArea.HarvestAreaDetailedType.getByName(e.getKey()), Map.Entry::getValue));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Map<HarvestArea.HarvestAreaDetailedType, Integer> countQuotasByArea(final int speciesCode, final int huntingYear) {
        final QHarvest HARVEST = QHarvest.harvest;
        final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
        final QHarvestQuota QUOTA = QHarvestQuota.harvestQuota;
        final QHarvestArea AREA = QHarvestArea.harvestArea;

        final DateTime huntingYearStart = DateUtil.toDateTimeNullSafe(DateUtil.huntingYearBeginDate(huntingYear));
        final DateTime huntingYearEnd = DateUtil.toDateTimeNullSafe(DateUtil.huntingYearEndDate(huntingYear)).plusDays(1).minusMillis(1);

        return jpqlQueryFactory.selectDistinct(QUOTA)
                .from(HARVEST)
                .innerJoin(HARVEST.species, SPECIES)
                .innerJoin(HARVEST.harvestQuota, QUOTA)
                .innerJoin(QUOTA.harvestArea, AREA)
                .where(SPECIES.officialCode.eq(speciesCode)
                        .and(HARVEST.pointOfTime.between(huntingYearStart, huntingYearEnd)))
                .transform(GroupBy.groupBy(AREA.nameFinnish).as(GroupBy.sum(QUOTA.quota)))
                .entrySet().stream()
                .collect(Collectors.toMap(e -> HarvestArea.HarvestAreaDetailedType.getByName(e.getKey()), Map.Entry::getValue));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Long> getHarvestIdsWhereOnlyAuthor(final Person person) {
        final QHarvest HARVEST = QHarvest.harvest;

        return jpqlQueryFactory.select(HARVEST.id)
                .from(HARVEST)
                .where(HARVEST.author.eq(person))
                .where(HARVEST.actualShooter.ne(person))
                .fetch();
    }
}
