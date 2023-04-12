package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import org.joda.time.LocalDate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Objects;

import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED;
import static fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventStatus.ACCEPTED_SUBSIDIZED;
import static java.util.Objects.requireNonNull;

@Component
public class MobileHuntingControlHelper {

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertValidGameWarden(final Riistanhoitoyhdistys rhy, final LocalDate date) {
        if (!userAuthorizationHelper.isGameWardenValidOn(rhy, date)) {
            throw new AccessDeniedException("User is not nominated as a game warden on the event date");
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void assertPersonIsEventInspector(final HuntingControlEvent event, final Person person) {
        if (Objects.nonNull(event) && !event.getInspectors().contains(person)) {
            throw new AccessDeniedException("User is not inspector of the event");
        }
    }

    public static void assertEventIsEditable(@Nonnull final HuntingControlEvent event) {
        requireNonNull(event);
        if (event.getStatus() == ACCEPTED || event.getStatus() == ACCEPTED_SUBSIDIZED) {
            throw new AccessDeniedException("User can not edit the event");
        }
    }

}
