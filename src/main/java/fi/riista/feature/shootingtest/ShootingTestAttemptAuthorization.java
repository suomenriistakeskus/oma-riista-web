package fi.riista.feature.shootingtest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.feature.shootingtest.AbstractShootingTestEntityAuthorization.Role.ASSIGNED_SHOOTING_TEST_OFFICIAL;
import static fi.riista.security.EntityPermission.CREATE;
import static fi.riista.security.EntityPermission.DELETE;
import static fi.riista.security.EntityPermission.READ;
import static fi.riista.security.EntityPermission.UPDATE;

@Component
public class ShootingTestAttemptAuthorization extends AbstractShootingTestEntityAuthorization<ShootingTestAttempt> {

    @Resource
    private JPAQueryFactory queryFactory;

    public ShootingTestAttemptAuthorization() {
        allow(READ, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, AMPUMAKOKEEN_VASTAANOTTAJA);
        allow(CREATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
        allow(UPDATE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
        allow(DELETE, ROLE_ADMIN, ROLE_MODERATOR, TOIMINNANOHJAAJA, ASSIGNED_SHOOTING_TEST_OFFICIAL);
    }

    @Override
    protected ShootingTestEvent getShootingTestEvent(final ShootingTestAttempt entity) {
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;

        return queryFactory.select(EVENT)
                .from(PARTICIPANT)
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT).fetchJoin()
                .where(PARTICIPANT.id.eq(entity.getParticipant().getId()))
                .fetchOne();
    }
}
