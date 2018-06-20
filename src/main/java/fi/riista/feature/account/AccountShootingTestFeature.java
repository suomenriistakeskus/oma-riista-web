package fi.riista.feature.account;

import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.shootingtest.QShootingTestAttempt;
import fi.riista.feature.shootingtest.QShootingTestEvent;
import fi.riista.feature.shootingtest.QShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTestAttempt;
import fi.riista.feature.shootingtest.ShootingTestAttemptResult;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class AccountShootingTestFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PersonRepository personRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public List<AccountShootingTestDTO> listMyShootingTests(final Long personId) {
        final SystemUser activeUser = activeUserService.requireActiveUser();
        Person person = activeUser.getPerson();

        if (person == null) {
            if (personId != null && activeUser.isModeratorOrAdmin()) {
                person = personRepository.getOne(personId);
            } else {
                return Collections.emptyList();
            }
        }
        return listQualifiedShootingTests(person, Locales.isSwedish(LocaleContextHolder.getLocale()));
    }

    private List<AccountShootingTestDTO> listQualifiedShootingTests(final Person person, final boolean swedish) {
        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QOrganisation ORG = QOrganisation.organisation;

        final StringPath namePath = swedish ? ORG.nameSwedish : ORG.nameFinnish;

        return jpqlQueryFactory
                .select(ATTEMPT.type, CALENDAR_EVENT.date, namePath)
                .from(PARTICIPANT)
                .join(PARTICIPANT.attempts, ATTEMPT).on(ATTEMPT.result.eq(ShootingTestAttemptResult.QUALIFIED))
                .join(PARTICIPANT.shootingTestEvent, EVENT)
                .join(EVENT.calendarEvent, CALENDAR_EVENT)
                .join(CALENDAR_EVENT.organisation, ORG)
                .where(PARTICIPANT.person.eq(person))
                .where(PARTICIPANT.completed.isTrue())
                .distinct()
                .fetch()
                .stream()
                .map(t -> {
                    final AccountShootingTestDTO dto = new AccountShootingTestDTO();

                    dto.setRhyName(t.get(namePath));
                    dto.setType(t.get(ATTEMPT.type));

                    final LocalDate date = DateUtil.toLocalDateNullSafe(t.get(CALENDAR_EVENT.date));
                    dto.setBegin(date);
                    dto.setEnd(date.plus(ShootingTestAttempt.SHOOTING_TEST_VALIDITY_PERIOD));
                    dto.setExpired(DateUtil.today().isAfter(dto.getEnd()));
                    return dto;
                })
                .collect(toList());
    }
}
