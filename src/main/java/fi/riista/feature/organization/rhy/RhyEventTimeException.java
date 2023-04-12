package fi.riista.feature.organization.rhy;

import fi.riista.feature.organization.rhy.annualstats.AnnualStatisticsService;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static fi.riista.util.DateUtil.today;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RhyEventTimeException extends RuntimeException {

    public RhyEventTimeException(final String msg) {
        super(msg);
    }

    public static void assertEventNotTooFarInPast(final LocalDate eventDate,
                                                  final boolean isModerator) {
        if (!isModerator && AnnualStatisticsService.hasDeadlinePassed(eventDate)) {
            throw new RhyEventTimeException("Event too far in the past.");
        }
    }

    public static void assertEventLastModificationTime(final LocalDate lastModificationDate,
                                                       final boolean isModerator) {
        final LocalDate today = today();

        if (!isModerator && today.isAfter(lastModificationDate)) {
            throw new RhyEventTimeException("Event's last modification time has passed.");
        }
    }

    public static void assertEventNotInFuture(final LocalDate eventDate) {
        final LocalDate today = today();

        if (eventDate.isAfter(today)) {
            throw new RhyEventTimeException( "Event in the future.");
        }
    }

    public static void assertBeginTimeNotAfterEndTime(final LocalTime beginTime,
                                                      final LocalTime endTime) {
        if (beginTime.isAfter(endTime)) {
            throw new RhyEventTimeException("Begin time must not be after end time");
        }
    }

}