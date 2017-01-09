package fi.riista.feature.gamediary.observation;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.spatial.GeometryExpression;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.sql.SQObservation;
import fi.riista.sql.SQGroupHuntingDay;
import fi.riista.sql.SQGroupObservationRejection;
import fi.riista.sql.SQHuntingClubArea;
import fi.riista.sql.SQOccupation;
import fi.riista.sql.SQOrganisation;
import fi.riista.sql.SQPerson;
import fi.riista.sql.SQZone;
import fi.riista.util.GISUtils;
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
    public List<Observation> findGroupObservations(
            final HuntingClubGroup huntingClubGroup, final Interval interval) {

        final SQObservation observation = new SQObservation("game_observation");
        final QObservation observationEntity = new QObservation("game_observation");

        final SubQueryExpression<Long> subQuery1 = gameObservationForGroupMemberInsideGroupHuntingArea(huntingClubGroup, interval);
        final SubQueryExpression<Long> subQuery2 = gameObservationLinkedToGroupHuntingDay(huntingClubGroup);
        final SubQueryExpression<Long> subQuery3 = gameObservationRejected(huntingClubGroup);

        return new JPASQLQuery<Observation>(entityManager, sqlTemplates)
                .select(observationEntity).from(observation)
                .where(observation.gameObservationId.in(union(asList(subQuery1, subQuery2, subQuery3))))
                .orderBy(observation.pointOfTime.desc(), observation.observerId.desc())
                .fetch();
    }

    private static SubQueryExpression<Long> gameObservationForGroupMemberInsideGroupHuntingArea(
            final HuntingClubGroup huntingClubGroup, final Interval interval) {
        /*
        SELECT o2.game_observation_id
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
          AND o2.within_moose_hunting = true
          AND o2.point_of_time >= :beginTime AND o2.point_of_time < :endTime
          AND ST_Intersects(z.geom, ST_SetSRID(ST_MakePoint(o2.longitude, o2.latitude), 3067))
        */
        final SQObservation gameObservation = new SQObservation("o2");
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

        return SQLExpressions.select(gameObservation.gameObservationId)
                .from(groupOccupation)
                .join(group).on(group.organisationId.eq(groupOccupation.organisationId)
                        .and(group.organisationType.eq(OrganisationType.CLUBGROUP.name())))
                .join(clubOccupation).on(clubOccupation.deletionTime.isNull()
                        .and(clubOccupation.personId.eq(groupOccupation.personId))
                        .and(clubOccupation.organisationId.eq(group.parentOrganisationId)))
                .join(huntingClubArea).on(huntingClubArea.huntingClubAreaId.eq(group.huntingAreaId))
                .join(zone).on(huntingClubArea.zoneId.eq(zone.zoneId))
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
                        .and(gameObservation.withinMooseHunting.isTrue())
                        .and(gameObservation.pointOfTime.between(
                                new Timestamp(interval.getStartMillis()),
                                new Timestamp(interval.getEndMillis())
                        ))
                        .and(zone.geom.intersects(getObservationPointGeometry(gameObservation))));
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
        final SQObservation gameObservation = new SQObservation("o3");
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

    private static GeometryExpression<?> getObservationPointGeometry(final SQObservation observation) {
        return GISUtils.createPointWithDefaultSRID(observation.longitude, observation.latitude);
    }
}
