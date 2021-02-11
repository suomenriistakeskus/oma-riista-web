package fi.riista.feature.gamediary.observation;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.sql.SQDeerPilot;
import fi.riista.sql.SQGameObservation;
import fi.riista.sql.SQGameSpecies;
import fi.riista.sql.SQGroupHuntingDay;
import fi.riista.sql.SQGroupObservationRejection;
import fi.riista.sql.SQHarvestPermitSpeciesAmount;
import fi.riista.sql.SQHuntingClubArea;
import fi.riista.sql.SQOccupation;
import fi.riista.sql.SQOrganisation;
import fi.riista.sql.SQPerson;
import fi.riista.sql.SQZone;
import fi.riista.util.DateUtil;
import fi.riista.util.GISUtils;
import org.geolatte.geom.Geometry;
import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import static com.querydsl.sql.SQLExpressions.union;
import static java.util.Arrays.asList;

@Transactional
@Repository
public class ObservationRepositoryImpl implements ObservationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private SQLTemplates sqlTemplates;

    @Transactional(readOnly = true)
    @Override
    public List<HuntingClubGroup> findGroupCandidatesForDeerObservation(final Observation observation) {
        final SQOrganisation group = new SQOrganisation("organisation");
        final SQOccupation occupation = new SQOccupation("occupation");
        final Date today = Date.valueOf(DateUtil.today().toString());
        final BooleanExpression membershipIsValid = occupation.endDate.gt(today).or(occupation.endDate.isNull());
        final SubQueryExpression<Long> allHuntingClubGroupsForObserver = SQLExpressions
                .select(occupation.organisationId)
                .from(occupation)
                .where(occupation.occupationType.in(
                        OccupationType.RYHMAN_METSASTYKSENJOHTAJA.toString(),
                        OccupationType.RYHMAN_JASEN.toString()),
                       // Author and observer are same for observations within deer hunting
                       occupation.personId.eq(observation.getAuthor().getId()),
                       // User must be member of the group when making new/editing existing observation
                       membershipIsValid);

        final QHuntingClubGroup huntingClubGroupEntity = new QHuntingClubGroup("organisation");
        final SQGameSpecies gameSpecies = new SQGameSpecies("game_species");
        final SQHuntingClubArea huntingClubArea = new SQHuntingClubArea("hunting_club_area");
        final SQZone zone = new SQZone("zone");
        final SQDeerPilot deerPilot = new SQDeerPilot("deer_pilot");
        final SQHarvestPermitSpeciesAmount speciesAmount = new SQHarvestPermitSpeciesAmount("species_amount");
        final Date pointOfTime = Date.valueOf(observation.getPointOfTimeAsLocalDate().toString());


        final BooleanBuilder withinDateLimits = new BooleanBuilder()
            .and(speciesAmount.beginDate.loe(pointOfTime).and(speciesAmount.endDate.goe(pointOfTime)))
            .orAllOf(speciesAmount.beginDate2.isNotNull(),
                     speciesAmount.endDate2.isNotNull(),
                     speciesAmount.beginDate2.loe(pointOfTime),
                     speciesAmount.endDate2.goe(pointOfTime));

        final SubQueryExpression<Long> matchingHuntingClubGroupsForObservation = SQLExpressions
                .select(group.organisationId)
                .from(group)
                .join(gameSpecies).on(gameSpecies.gameSpeciesId.eq(group.gameSpeciesId))
                .join(huntingClubArea).on(huntingClubArea.huntingClubAreaId.eq(group.huntingAreaId))
                .join(zone).on(zone.zoneId.eq(huntingClubArea.zoneId))
                .join(deerPilot).on(deerPilot.harvestPermitId.eq(group.harvestPermitId))
                .join(speciesAmount).on(speciesAmount.harvestPermitId.eq(group.harvestPermitId))
                // Organisation is hunting group
                .where(group.organisationType.eq(OrganisationType.CLUBGROUP.toString()),
                       // ... and the observer is a member
                       group.organisationId.in(allHuntingClubGroupsForObserver),
                       // ... and the group is active
                       group.active.isTrue(),
                       // ... and it's for white tailed deer
                       gameSpecies.officialCode.eq(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER),
                       speciesAmount.gameSpeciesId.eq(gameSpecies.gameSpeciesId),
                       // ... and the permit is valid on the day of observation
                       withinDateLimits,
                       // ... and the observation is inside the group's area
                       zone.geom.intersects(GISUtils.createPointWithDefaultSRID(observation.getGeoLocation()))
                       );

        final BooleanExpression observationIsNotRejectedByTheGroup = observation.isNew()
                ? null
                : isDeerObservationRejected(observation, matchingHuntingClubGroupsForObservation);

        // Fetch all matching groups except the ones where observation is rejected.
        final List<HuntingClubGroup> huntingClubGroups = new JPASQLQuery<HuntingClubGroup>(entityManager, sqlTemplates)
                .select(huntingClubGroupEntity)
                .from(group)
                .where(group.organisationId.in(matchingHuntingClubGroupsForObservation),
                       observationIsNotRejectedByTheGroup)
                .fetch();

        return huntingClubGroups;
    }

    private static BooleanExpression isDeerObservationRejected(
            final Observation observation, final SubQueryExpression<Long> matchingHuntingClubGroupsForObservation) {

        final SQOrganisation group = new SQOrganisation("organisation");
        final SQGroupObservationRejection rejection = new SQGroupObservationRejection("group_observation_rejection");

        final SubQueryExpression<Long> observationRejectedInGroups = SQLExpressions
                .select(rejection.huntingClubGroupId)
                .from(rejection)
                .where(rejection.huntingClubGroupId.in(matchingHuntingClubGroupsForObservation),
                       rejection.observationId.eq(observation.getId()));

        return group.organisationId.notIn(observationRejectedInGroups);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Observation> findGroupObservations(
            final HuntingClubGroup huntingClubGroup, final ObservationCategory observationCategory, final Interval interval) {

        final SQGameObservation observation = new SQGameObservation("game_observation");
        final QObservation observationEntity = new QObservation("game_observation");

        final SubQueryExpression<Long> subQuery1 = gameObservationForGroupMemberInsideGroupHuntingArea(
                huntingClubGroup, observationCategory, interval);
        final SubQueryExpression<Long> subQuery2 = gameObservationLinkedToGroupHuntingDay(huntingClubGroup);
        final SubQueryExpression<Long> subQuery3 = gameObservationRejected(huntingClubGroup);

        return new JPASQLQuery<Observation>(entityManager, sqlTemplates)
                .select(observationEntity).from(observation)
                .where(observation.gameObservationId.in(union(asList(subQuery1, subQuery2, subQuery3))))
                .orderBy(observation.pointOfTime.desc(), observation.observerId.desc())
                .fetch();
    }

    private static SubQueryExpression<Long> gameObservationForGroupMemberInsideGroupHuntingArea(
            final HuntingClubGroup huntingClubGroup, final ObservationCategory observationCategory, final Interval interval) {
        /*
        SELECT sq.game_observation_id FROM (
        SELECT DISTINCT a.zone_id, o2.game_observation_id, o2.geom as geom
        FROM occupation groupOcc
        INNER JOIN organisation g ON (g.organisation_id = groupOcc.organisation_id AND g.organisation_type = 'CLUBGROUP')
        INNER JOIN occupation clubOcc ON (clubOcc.deletion_time IS NULL AND clubOcc.person_id = groupOcc.person_id AND clubOcc.organisation_id = g.parent_organisation_id)
        INNER JOIN hunting_club_area a ON (a.hunting_club_area_id = g.hunting_area_id)
        INNER JOIN zone z ON (z.zone_id = a.zone_id)
        INNER JOIN person p ON (p.person_id = groupOcc.person_id)
        INNER JOIN game_observation o2 ON p.person_id IN (o2.author_id, o2.observer_id)
        WHERE
          groupOcc.organisation_id = :huntingGroupId
          AND groupOcc.deletion_time IS NULL
          AND o2.point_of_time BETWEEN COALESCE(groupOcc.begin_date, o2.point_of_time) AND COALESCE(groupOcc.end_date, o2.point_of_time)
          AND o2.point_of_time BETWEEN COALESCE(clubOcc.begin_date, o2.point_of_time) AND COALESCE(clubOcc.end_date, o2.point_of_time)
          AND o2.group_hunting_day_id IS NULL
          AND o2.observation_category = :observationCategory
          AND o2.point_of_time >= :beginTime AND o2.point_of_time < :endTime
        ) sq JOIN zone z ON z.zone_id = sq.zone_id
        AND ST_Intersects(z.geom, sq.geom)
        */
        final SQGameObservation gameObservation = new SQGameObservation("o2");
        final SQOrganisation group = new SQOrganisation("g");
        final SQOccupation groupOccupation = new SQOccupation("groupOcc");
        final SQOccupation clubOccupation = new SQOccupation("clubOcc");
        final SQHuntingClubArea huntingClubArea = new SQHuntingClubArea("a");
        final SQZone zone = new SQZone("z");
        final SQPerson person = new SQPerson("p");

        final DateExpression<Date> gameObservationDate =
                SQLExpressions.date(java.sql.Date.class, gameObservation.pointOfTime);

        final BooleanExpression authorOrObserver = person.personId.eq(gameObservation.authorId)
                .or(person.personId.eq(gameObservation.observerId));

        final SQLQuery<Tuple> subQuery = SQLExpressions
                .select(huntingClubArea.zoneId, gameObservation.gameObservationId, gameObservation.geom.as("geom"))
                .distinct()
                .from(groupOccupation)
                .join(group).on(group.organisationId.eq(groupOccupation.organisationId)
                        .and(group.organisationType.eq(OrganisationType.CLUBGROUP.name())))
                .join(clubOccupation).on(clubOccupation.deletionTime.isNull()
                        .and(clubOccupation.personId.eq(groupOccupation.personId))
                        .and(clubOccupation.organisationId.eq(group.parentOrganisationId)))
                .join(huntingClubArea).on(huntingClubArea.huntingClubAreaId.eq(group.huntingAreaId))
                .join(person).on(person.personId.eq(groupOccupation.personId))
                .join(gameObservation).on(authorOrObserver)
                .where(groupOccupation.organisationId.eq(huntingClubGroup.getId())
                        .and(groupOccupation.deletionTime.isNull())
                        .and(gameObservationDate.between(
                                groupOccupation.beginDate.coalesce(gameObservationDate),
                                groupOccupation.endDate.coalesce(gameObservationDate)))
                        .and(gameObservationDate.between(
                                clubOccupation.beginDate.coalesce(gameObservationDate),
                                clubOccupation.endDate.coalesce(gameObservationDate)))
                        .and(gameObservation.groupHuntingDayId.isNull())
                        .and(gameObservation.observationCategory.eq(observationCategory.getDatabaseValue()))
                        .and(gameObservation.pointOfTime.between(
                                new Timestamp(interval.getStartMillis()),
                                new Timestamp(interval.getEndMillis())
                        )));

        final PathBuilder<Object[]> sq = new PathBuilder<>(Object[].class, "sq");
        final PathBuilder<Geometry> sqGeomPath = sq.get("geom", Geometry.class);
        final GeometryExpression<Geometry> sqGeom = GeometryExpressions.asGeometry(sqGeomPath);

        return SQLExpressions
                .select(sq.get("game_observation_id", Long.class))
                .from(subQuery.as("sq"))
                .join(zone).on(zone.zoneId.eq(sq.get("zone_id", Long.class)))
                .where(zone.geom.intersects(sqGeom));
    }

    private static SubQueryExpression<Long> gameObservationLinkedToGroupHuntingDay(
            final HuntingClubGroup huntingClubGroup) {
        /*
        SELECT o3.game_observation_id
        FROM game_observation o3
        WHERE o3.group_hunting_day_id IN (
          SELECT group_hunting_day_id
          FROM group_hunting_day
          WHERE hunting_group_id = :huntingGroupId)
        */
        final SQGameObservation gameObservation = new SQGameObservation("o3");
        final SQGroupHuntingDay groupHuntingDay = SQGroupHuntingDay.groupHuntingDay;

        final SQLQuery<Long> groupHuntingDayIds = SQLExpressions.selectOne()
                .from(groupHuntingDay)
                .where(groupHuntingDay.huntingGroupId.eq(huntingClubGroup.getId()))
                .select(groupHuntingDay.groupHuntingDayId);

        return SQLExpressions.select(gameObservation.gameObservationId)
                .from(gameObservation)
                .where(gameObservation.groupHuntingDayId.in(groupHuntingDayIds));
    }

    private static SubQueryExpression<Long> gameObservationRejected(HuntingClubGroup huntingClubGroup) {
        final SQGroupObservationRejection rejection = SQGroupObservationRejection.groupObservationRejection;
        return SQLExpressions.select(rejection.observationId)
                .from(rejection)
                .where(rejection.huntingClubGroupId.eq(huntingClubGroup.getId()));
    }
}
