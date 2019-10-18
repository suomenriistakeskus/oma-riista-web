package fi.riista.feature.account;

import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.calendar.QCalendarEvent;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.shootingtest.QShootingTestAttempt;
import fi.riista.feature.shootingtest.QShootingTestEvent;
import fi.riista.feature.shootingtest.QShootingTestParticipant;
import fi.riista.feature.shootingtest.ShootingTest;
import fi.riista.feature.shootingtest.ShootingTestAttemptResult;
import fi.riista.feature.shootingtest.ShootingTestType;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fi.riista.util.Collect.greatestAfterGroupingBy;
import static fi.riista.util.DateUtil.today;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class AccountShootingTestService {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private EnumLocaliser enumLocaliser;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<AccountShootingTestDTO> listQualifiedShootingTests(final Person person, final Locale locale) {
        final LocalDate today = today();

        final QShootingTestParticipant PARTICIPANT = QShootingTestParticipant.shootingTestParticipant;
        final QShootingTestAttempt ATTEMPT = QShootingTestAttempt.shootingTestAttempt;
        final QShootingTestEvent EVENT = QShootingTestEvent.shootingTestEvent;
        final QCalendarEvent CALENDAR_EVENT = QCalendarEvent.calendarEvent;
        final QOrganisation ORG = QOrganisation.organisation;

        final StringPath rhyNamePath = Locales.isSwedish(locale) ? ORG.nameSwedish : ORG.nameFinnish;

        final Comparator<AccountShootingTestDTO> temporalOrdering = comparing(AccountShootingTestDTO::getBegin);

        // Most recent shooting test is collected for each type.
        final Map<ShootingTestType, AccountShootingTestDTO> grouping = jpqlQueryFactory
                .select(ATTEMPT.type, CALENDAR_EVENT.date, ORG.officialCode, rhyNamePath)
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

                    dto.setRhyCode(t.get(ORG.officialCode));
                    dto.setRhyName(t.get(rhyNamePath));

                    final ShootingTestType type = t.get(ATTEMPT.type);
                    final LocalisedString typeName = enumLocaliser.getLocalisedString(type);

                    dto.setType(type);
                    dto.setTypeName(typeName.getAnyTranslation(locale));

                    final LocalDate beginDate = DateUtil.toLocalDateNullSafe(t.get(CALENDAR_EVENT.date));
                    final LocalDate endDate = beginDate.plus(ShootingTest.VALIDITY_PERIOD);

                    dto.setBegin(beginDate);
                    dto.setEnd(endDate);
                    dto.setExpired(today.isAfter(endDate));

                    return dto;
                })
                .collect(greatestAfterGroupingBy(AccountShootingTestDTO::getType, temporalOrdering));

        return grouping.values()
                .stream()
                .sorted(temporalOrdering.reversed().thenComparing(AccountShootingTestDTO::getType))
                .collect(toList());
    }
}
