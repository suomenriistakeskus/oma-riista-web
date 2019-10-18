package fi.riista.feature.shootingtest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.shootingtest.AbstractShootingTestEntityAuthorization.Role.ASSIGNED_SHOOTING_TEST_OFFICIAL;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class ShootingTestParticipantAuthorization
        extends AbstractShootingTestEntityAuthorization<ShootingTestParticipant> {

    @Resource
    private JPAQueryFactory queryFactory;

    public ShootingTestParticipantAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
    }

    @Override
    protected ShootingTestEvent getShootingTestEvent(final ShootingTestParticipant entity) {
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;

        return queryFactory.select(EVENT)
                .from(EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT).fetchJoin()
                .where(EVENT.id.eq(entity.getShootingTestEvent().getId()))
                .fetchOne();
    }
}
